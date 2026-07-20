package lol.hisoka.manifold.graph;

import lol.hisoka.manifold.api.endpoint.Endpoint;
import lol.hisoka.manifold.api.endpoint.EndpointType;
import lol.hisoka.manifold.api.event.RequestFulfillmentEvent;
import lol.hisoka.manifold.api.network.*;
import lol.hisoka.manifold.block.AbstractPipeBlock;
import lol.hisoka.manifold.capability.CapabilityEndpoint;
import lol.hisoka.manifold.capability.EndpointAdapterRegistry;
import lol.hisoka.manifold.capability.InventoryPoller;
import lol.hisoka.manifold.energy.EnergyBudget;
import lol.hisoka.manifold.routing.ItemTransitManager;
import lol.hisoka.manifold.routing.RequestQueue;
import lol.hisoka.manifold.routing.RoutingTable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class NetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private static final int COALESCING_TICKS = 4;
    private static final int POLL_INTERVAL_TICKS = 20;

    private final NetworkGraph networkGraph;
    private final RoutingTable routingTable;
    private final RequestQueue requestQueue;
    private final CraftingTable craftingTable;
    private final EnergyBudget energyBudget;
    private final InventoryPoller inventoryPoller;
    private final ItemTransitManager transitManager;

    private final Map<NetworkId, Long> dirtyCoalesceTimers = new HashMap<>();
    private final Map<NetworkId, Integer> tickCounters = new HashMap<>();
    private final Map<NetworkNode, EndpointType> nodePipeTypes = new HashMap<>();
    private int globalTickCounter;

    private static NetworkManager instance;

    private NetworkManager(NetworkGraph networkGraph, RoutingTable routingTable,
                           RequestQueue requestQueue, CraftingTable craftingTable,
                           EnergyBudget energyBudget) {
        this.networkGraph = networkGraph;
        this.routingTable = routingTable;
        this.requestQueue = requestQueue;
        this.craftingTable = craftingTable;
        this.energyBudget = energyBudget;
        this.inventoryPoller = new InventoryPoller();
        this.transitManager = new ItemTransitManager();
    }

    public static void init(NetworkGraph networkGraph, RoutingTable routingTable,
                            RequestQueue requestQueue, CraftingTable craftingTable,
                            EnergyBudget energyBudget) {
        instance = new NetworkManager(networkGraph, routingTable, requestQueue, craftingTable, energyBudget);
    }

    public static NetworkManager get() {
        return instance;
    }

    public void onBlockPlaced(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) return;

        BlockState state = level.getBlockState(pos);
        EndpointType pipeType = detectPipeType(state);

        NetworkNode.Junction junction = new NetworkNode.Junction(nodeId(pos));
        NetworkId assignedNetwork = null;

        for (Direction side : Direction.values()) {
            BlockPos neighborPos = pos.relative(side);
            Optional<Endpoint> neighborEndpoint = EndpointAdapterRegistry.get()
                    .tryCreate(level, neighborPos, side.getOpposite(), pipeType);

            if (neighborEndpoint.isPresent()) {
                Endpoint endpoint = neighborEndpoint.get();
                NetworkNode endNode = new NetworkNode.EndpointWrapper(endpoint);

                if (networkGraph.getNetworkFor(endNode).isEmpty()) {
                    networkGraph.addNode(endNode, NetworkId.create());
                }
                Optional<NetworkId> neighborNetwork = networkGraph.getNetworkFor(endNode);

                if (assignedNetwork == null) {
                    assignedNetwork = neighborNetwork.orElseThrow(
                            () -> new IllegalStateException("Network not found for node after add"));
                    networkGraph.addNode(junction, assignedNetwork);
                } else if (!assignedNetwork.equals(neighborNetwork.orElseThrow(
                        () -> new IllegalStateException("Network not found for neighbor node")))) {
                    networkGraph.mergeNetworks(neighborNetwork.orElseThrow(), assignedNetwork);
                }

                NetworkEdge edge = new NetworkEdge(junction, endNode, 1);
                networkGraph.addEdge(edge, assignedNetwork);
            }
        }

        if (assignedNetwork == null) {
            assignedNetwork = NetworkId.create();
            networkGraph.addNode(junction, assignedNetwork);
        }

        if (pipeType != EndpointType.TRANSPORT) {
            nodePipeTypes.put(junction, pipeType);
        }

        markDirty(assignedNetwork);
        LOGGER.debug("Block placed at {} (type={}, network={})", pos, pipeType, assignedNetwork);
    }

    public void onBlockRemoved(Level level, BlockPos pos) {
        String id = nodeId(pos);
        Optional<NetworkNode> nodeOpt = networkGraph.getNode(id);
        if (nodeOpt.isEmpty()) return;

        NetworkNode node = nodeOpt.get();
        Optional<NetworkId> networkOpt = networkGraph.getNetworkFor(node);
        if (networkOpt.isEmpty()) return;

        NetworkId networkId = networkOpt.get();

        for (NetworkNode n : networkGraph.getNodes(networkId)) {
            if (n instanceof NetworkNode.EndpointWrapper wrapper
                    && wrapper.endpoint() instanceof CapabilityEndpoint capEp) {
                inventoryPoller.removeEndpoint(capEp.id());
            }
        }

        List<NetworkGraph.NetworkSplitResult> splits = networkGraph.splitNetwork(networkId, node);

        networkGraph.removeNode(node);
        nodePipeTypes.remove(node);

        markDirty(networkId);
        for (NetworkGraph.NetworkSplitResult split : splits) {
            markDirty(split.newNetworkId());
        }

        routingTable.invalidate(networkId);
        requestQueue.clear(networkId);
        craftingTable.clear(networkId);
        energyBudget.clear(networkId);
        inventoryPoller.clearAll();
    }

    public void serverTick(ServerLevel level) {
        globalTickCounter++;

        var completedItems = transitManager.tickAll(level);
        for (var item : completedItems) {
            deliverItem(level, item);
        }

        Set<NetworkId> allNetworks = new HashSet<>(networkGraph.allNetworkIds());
        for (NetworkId networkId : allNetworks) {
            tickCounters.merge(networkId, 1, (prev, one) -> prev + 1);

            processCoalescing(networkId);
            pollProviderInventories(level, networkId);

            Set<NetworkNode> nodes = networkGraph.getNodes(networkId);
            energyBudget.scanAndCollect(networkId, nodes, level, globalTickCounter);

            Queue<RequestQueue.PendingRequest> mailbox = requestQueue.drainMailbox(networkId);
            processRequests(networkId, mailbox);
        }
    }

    private void pollProviderInventories(ServerLevel level, NetworkId networkId) {
        int networkTick = tickCounters.getOrDefault(networkId, 0);
        if (networkTick % POLL_INTERVAL_TICKS != 0) return;

        Set<NetworkNode> nodes = networkGraph.getNodes(networkId);
        for (NetworkNode node : nodes) {
            if (!(node instanceof NetworkNode.EndpointWrapper wrapper)) continue;

            Endpoint endpoint = wrapper.endpoint();
            if (!endpoint.isProvider() || !(endpoint instanceof CapabilityEndpoint capEndpoint)) {
                continue;
            }

            IItemHandler handler = capEndpoint.getItemHandler();
            if (handler == null) continue;

            InventoryPoller.PollResult result = inventoryPoller.poll(
                    endpoint.id(), handler, globalTickCounter
            );

            if (result.changed()) {
                LOGGER.debug("Inventory changed at endpoint {} on network {}",
                        endpoint.id(), networkId);
                markDirtyOnInventoryChange(networkId);
            }
        }
    }

    public void markDirty(NetworkId networkId) {
        dirtyCoalesceTimers.put(networkId, (long) globalTickCounter + COALESCING_TICKS);
    }

    public void markDirtyOnInventoryChange(NetworkId networkId) {
        long expireAt = dirtyCoalesceTimers.getOrDefault(networkId, 0L);
        if (globalTickCounter >= expireAt) {
            routingTable.markDirty(networkId);
            triggerAsyncRecompute(networkId);
            dirtyCoalesceTimers.remove(networkId);
        }
    }

    private void processCoalescing(NetworkId networkId) {
        long expireAt = dirtyCoalesceTimers.getOrDefault(networkId, 0L);
        if (expireAt > 0 && globalTickCounter >= expireAt) {
            dirtyCoalesceTimers.remove(networkId);
            routingTable.markDirty(networkId);
            triggerAsyncRecompute(networkId);
        }
    }

    private void triggerAsyncRecompute(NetworkId networkId) {
        Set<NetworkNode> nodes = networkGraph.getNodes(networkId);
        boolean hasRequester = false;
        for (NetworkNode node : nodes) {
            if (node instanceof NetworkNode.EndpointWrapper wrapper
                    && wrapper.endpoint().isRequester()) {
                routingTable.recomputeAsync(networkId, node);
                hasRequester = true;
                break;
            }
        }
        if (!hasRequester) {
            for (NetworkNode node : nodes) {
                if (node.isJunction()) {
                    routingTable.recomputeAsync(networkId, node);
                    break;
                }
            }
        }
    }

    private void processRequests(NetworkId networkId, Queue<RequestQueue.PendingRequest> mailbox) {
        for (RequestQueue.PendingRequest request : mailbox) {
            String sourceNodeId = findJunctionNodeId(networkId, request.requester());
            Map<NetworkNode, List<NetworkEdge>> routes = routingTable.getCached(networkId, sourceNodeId);

            if (routes.isEmpty()) {
                requestQueue.enqueue(networkId, request.requester(),
                        request.requested(), request.count());
                continue;
            }

            List<Map.Entry<NetworkNode, List<NetworkEdge>>> sortedRoutes = new ArrayList<>(routes.entrySet());
            sortedRoutes.sort((a, b) -> {
                int weightA = a.getValue().stream().mapToInt(NetworkEdge::weight).sum();
                int weightB = b.getValue().stream().mapToInt(NetworkEdge::weight).sum();
                if (weightA != weightB) return Integer.compare(weightA, weightB);
                int stockA = getStock(a.getKey(), request.requested());
                int stockB = getStock(b.getKey(), request.requested());
                return Integer.compare(stockB, stockA);
            });

            for (var entry : sortedRoutes) {
                NetworkNode target = entry.getKey();
                if (!(target instanceof NetworkNode.EndpointWrapper wrapper)
                        || !wrapper.endpoint().isProvider()
                        || !(wrapper.endpoint() instanceof CapabilityEndpoint capEndpoint)) {
                    continue;
                }

                IItemHandler providerHandler = capEndpoint.getItemHandler();
                if (providerHandler == null) continue;

                int pathWeight = entry.getValue().stream()
                        .mapToInt(NetworkEdge::weight).sum();

                boolean hasEnergy = energyBudget.consume(networkId, pathWeight);
                if (!hasEnergy) {
                    int degradation = energyBudget.getDegradationMultiplier(networkId);
                    if (degradation > 1) {
                        continue;
                    }
                }

                ItemStack extracted = extractFromHandler(providerHandler, request.requested(), request.count());
                if (extracted.isEmpty()) {
                    continue;
                }

                RequestFulfillmentEvent.Pre preEvent = new RequestFulfillmentEvent.Pre(
                        request.requester(), request.requested()
                );
                if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) {
                    continue;
                }

                UUID transitId = UUID.randomUUID();
                transitManager.startTransit(transitId, entry.getValue(), extracted, networkId, target);

                NeoForge.EVENT_BUS.post(new RequestFulfillmentEvent.Post(
                        request.requester(), request.requested(), true, wrapper.endpoint().id()
                ));

                LOGGER.debug("Routing {}x {} to provider {} on network {} (cost: {})",
                        request.count(), request.requested().getDisplayName().getString(),
                        wrapper.endpoint().id(), networkId, pathWeight);
                break;
            }
        }
    }

    private int getStock(NetworkNode node, ItemStack wanted) {
        if (!(node instanceof NetworkNode.EndpointWrapper wrapper)) return 0;
        var ads = inventoryPoller.getCached(wrapper.endpoint().id());
        if (ads == null) return 0;
        return ads.stream()
                .filter(a -> ItemStack.isSameItemSameComponents(a.stack(), wanted))
                .mapToInt(ItemAdvertisement::count)
                .sum();
    }

    private String findJunctionNodeId(NetworkId networkId, EndpointId requesterId) {
        BlockPos pos = requesterId.pos();
        return "pipe:%d,%d,%d".formatted(pos.getX(), pos.getY(), pos.getZ());
    }

    private ItemStack extractFromHandler(IItemHandler handler, ItemStack wanted, int count) {
        int remaining = count;
        for (int i = 0; i < handler.getSlots() && remaining > 0; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(slotStack, wanted)) {
                ItemStack extracted = handler.extractItem(i, remaining, false);
                remaining -= extracted.getCount();
                if (remaining <= 0) {
                    return wanted.copyWithCount(count);
                }
            }
        }
        int extractedCount = count - remaining;
        return extractedCount > 0 ? wanted.copyWithCount(extractedCount) : ItemStack.EMPTY;
    }

    private void deliverItem(ServerLevel level, ItemTransitManager.InTransitItem item) {
        NetworkNode target = item.targetNode();
        if (!(target instanceof NetworkNode.EndpointWrapper wrapper)) return;
        if (!(wrapper.endpoint() instanceof CapabilityEndpoint capEndpoint)) return;

        IItemHandler handler = capEndpoint.getItemHandler();
        if (handler == null) return;

        ItemStack remainder = ItemStack.EMPTY;
        for (int i = 0; i < handler.getSlots() && !item.stack().isEmpty(); i++) {
            remainder = handler.insertItem(i, item.stack(), false);
            if (remainder.isEmpty()) break;
        }

        if (!remainder.isEmpty()) {
            LOGGER.warn("Target inventory full, {} lost in transit", item.stack().getDisplayName().getString());
        }
    }

    @Nullable
    public EndpointType getPipeType(NetworkNode node) {
        return nodePipeTypes.get(node);
    }

    private static String nodeId(BlockPos pos) {
        return "pipe:%d,%d,%d".formatted(pos.getX(), pos.getY(), pos.getZ());
    }

    private static EndpointType detectPipeType(BlockState state) {
        if (state.getBlock() instanceof AbstractPipeBlock pipeBlock) {
            return pipeBlock.getPipeType();
        }
        return EndpointType.TRANSPORT;
    }

    public NetworkGraph networkGraph() {
        return networkGraph;
    }

    public RoutingTable routingTable() {
        return routingTable;
    }

    public RequestQueue requestQueue() {
        return requestQueue;
    }

    public CraftingTable craftingTable() {
        return craftingTable;
    }

    public EnergyBudget energyBudget() {
        return energyBudget;
    }

    public InventoryPoller inventoryPoller() {
        return inventoryPoller;
    }

    public ItemTransitManager transitManager() {
        return transitManager;
    }
}
