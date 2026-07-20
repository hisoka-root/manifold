package lol.hisoka.manifold.blockentity;

import lol.hisoka.manifold.api.endpoint.EndpointType;
import lol.hisoka.manifold.api.network.EndpointId;
import lol.hisoka.manifold.block.AbstractPipeBlock;
import lol.hisoka.manifold.graph.NetworkManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PipeBlockEntity extends BlockEntity {

    private static BlockEntityType<PipeBlockEntity> TYPE;

    private EndpointType pipeType = EndpointType.TRANSPORT;
    private UUID nonce;
    private EndpointId endpointId;
    private final Set<Direction> connectedSides = EnumSet.noneOf(Direction.class);

    private List<ItemStack> filter = new ArrayList<>();
    private Map<String, Integer> bufferTargets = new LinkedHashMap<>();
    private int requestCooldown;

    public PipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(TYPE, pos, blockState);
        this.nonce = UUID.randomUUID();
        if (blockState.getBlock() instanceof AbstractPipeBlock pipeBlock) {
            this.pipeType = pipeBlock.getPipeType();
        }
    }

    public static void setType(BlockEntityType<PipeBlockEntity> type) {
        TYPE = type;
    }

    public EndpointType getPipeType() {
        return pipeType;
    }

    public UUID getNonce() {
        return nonce;
    }

    public EndpointId getEndpointId() {
        if (endpointId == null) {
            endpointId = new EndpointId(worldPosition, nonce);
        }
        return endpointId;
    }

    public void discoverConnections(Level level) {
        BlockState myState = getBlockState();
        for (Direction side : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(side);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (canConnectTo(level, neighborPos, neighborState, side)) {
                connectSide(side);
            } else {
                disconnectSide(side);
            }
        }
    }

    public boolean canConnectTo(Level level, BlockPos neighborPos, BlockState neighborState, Direction side) {
        if (neighborState.getBlock() instanceof AbstractPipeBlock) {
            return true;
        }
        if (pipeType == EndpointType.TRANSPORT) {
            return false;
        }
        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, neighborPos, side.getOpposite()
        );
        return handler != null && handler.getSlots() > 0;
    }

    public void connectSide(Direction side) {
        if (connectedSides.add(side)) {
            if (level != null && !level.isClientSide && getBlockState().getBlock() instanceof AbstractPipeBlock) {
                BlockState newState = getBlockState().setValue(AbstractPipeBlock.propertyFor(side), true);
                level.setBlock(worldPosition, newState, 3);
            }
            setChanged();
            syncToClient();
        }
    }

    public void disconnectSide(Direction side) {
        if (connectedSides.remove(side)) {
            if (level != null && !level.isClientSide && getBlockState().getBlock() instanceof AbstractPipeBlock) {
                BlockState newState = getBlockState().setValue(AbstractPipeBlock.propertyFor(side), false);
                level.setBlock(worldPosition, newState, 3);
            }
            setChanged();
            syncToClient();
        }
    }

    public boolean isConnected(Direction side) {
        return connectedSides.contains(side);
    }

    public Set<Direction> connectedSides() {
        return EnumSet.copyOf(connectedSides);
    }

    public List<ItemStack> getFilter() {
        return Collections.unmodifiableList(filter);
    }

    public void setFilter(List<ItemStack> newFilter) {
        this.filter = new ArrayList<>(newFilter);
        setChanged();
    }

    public Map<String, Integer> getBufferTargets() {
        return Collections.unmodifiableMap(bufferTargets);
    }

    public void setBufferTarget(String itemKey, int target) {
        bufferTargets.put(itemKey, target);
        setChanged();
    }

    public void removeBufferTarget(String itemKey) {
        bufferTargets.remove(itemKey);
        setChanged();
    }

    @Nullable
    public IItemHandler getNeighborInventory(Level level) {
        Direction facing = getFacing();
        if (facing == null || !isConnected(facing)) return null;
        BlockPos neighborPos = worldPosition.relative(facing);
        return level.getCapability(
                Capabilities.ItemHandler.BLOCK, neighborPos, facing.getOpposite()
        );
    }

    @Nullable
    private Direction getFacing() {
        for (Direction side : connectedSides) {
            if (side != Direction.UP && side != Direction.DOWN) return side;
        }
        for (Direction side : connectedSides) {
            return side;
        }
        return null;
    }

    public void tick(Level level) {
        switch (pipeType) {
            case PROVIDER -> tickProvider(level);
            case REQUESTER -> tickRequester(level);
            case SUPPLIER -> tickSupplier(level);
            default -> {}
        }
    }

    private void tickProvider(Level level) {
    }

    private void tickRequester(Level level) {
        if (filter.isEmpty() || requestCooldown > 0) {
            requestCooldown = Math.max(0, requestCooldown - 1);
            return;
        }
        NetworkManager manager = NetworkManager.get();
        if (manager == null) return;
        for (ItemStack wanted : filter) {
            if (wanted.isEmpty()) continue;
            var networkOpt = manager.networkGraph().getNetworkFor(
                    new lol.hisoka.manifold.api.network.NetworkNode.Junction("pipe:%d,%d,%d".formatted(
                            worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()))
            );
            if (networkOpt.isPresent()) {
                manager.requestQueue().enqueue(networkOpt.get(), getEndpointId(), wanted.copy(),
                        wanted.getCount());
            }
        }
        requestCooldown = 10;
    }

    private void tickSupplier(Level level) {
        if (bufferTargets.isEmpty()) return;
        IItemHandler neighbor = getNeighborInventory(level);
        if (neighbor == null) return;
        NetworkManager manager = NetworkManager.get();
        if (manager == null) return;
        for (var entry : bufferTargets.entrySet()) {
            String targetKey = entry.getKey();
            int desired = entry.getValue();
            int current = countInInventory(neighbor, targetKey);
            if (current < desired) {
                int deficit = desired - current;
                var networkOpt = manager.networkGraph().getNetworkFor(
                        new lol.hisoka.manifold.api.network.NetworkNode.Junction("pipe:%d,%d,%d".formatted(
                                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()))
                );
                if (networkOpt.isPresent()) {
                    manager.requestQueue().enqueue(networkOpt.get(), getEndpointId(),
                            new ItemStack(findItemByKey(neighbor, targetKey).getItem(), deficit), deficit);
                }
            }
        }
    }

    private static int countInInventory(IItemHandler handler, String itemKey) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stackKey(stack).equals(itemKey)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ItemStack findItemByKey(IItemHandler handler, String itemKey) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stackKey(stack).equals(itemKey)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String stackKey(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("nonce")) {
            this.nonce = tag.getUUID("nonce");
        }
        if (tag.contains("pipeType")) {
            this.pipeType = EndpointType.valueOf(tag.getString("pipeType"));
        }
        connectedSides.clear();
        if (tag.contains("connectedSides")) {
            CompoundTag sidesTag = tag.getCompound("connectedSides");
            for (Direction side : Direction.values()) {
                if (sidesTag.getBoolean(side.getName())) {
                    connectedSides.add(side);
                }
            }
        }
        filter.clear();
        if (tag.contains("filter")) {
            ListTag filterTag = tag.getList("filter", Tag.TAG_COMPOUND);
            for (int i = 0; i < filterTag.size(); i++) {
                filter.add(ItemStack.parseOptional(registries, filterTag.getCompound(i)));
            }
        }
        bufferTargets.clear();
        if (tag.contains("bufferTargets")) {
            CompoundTag btTag = tag.getCompound("bufferTargets");
            for (String key : btTag.getAllKeys()) {
                bufferTargets.put(key, btTag.getInt(key));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("nonce", nonce);
        tag.putString("pipeType", pipeType.name());
        CompoundTag sidesTag = new CompoundTag();
        for (Direction side : Direction.values()) {
            sidesTag.putBoolean(side.getName(), connectedSides.contains(side));
        }
        tag.put("connectedSides", sidesTag);
        ListTag filterTag = new ListTag();
        for (ItemStack stack : filter) {
            filterTag.add(stack.save(registries));
        }
        tag.put("filter", filterTag);
        CompoundTag btTag = new CompoundTag();
        for (var entry : bufferTargets.entrySet()) {
            btTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("bufferTargets", btTag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
