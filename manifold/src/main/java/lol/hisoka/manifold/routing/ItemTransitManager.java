package lol.hisoka.manifold.routing;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ItemTransitManager {

    private final ConcurrentMap<UUID, InTransitItem> items = new ConcurrentHashMap<>();

    public void startTransit(UUID id, List<NetworkEdge> path, ItemStack stack, NetworkId networkId,
                              NetworkNode targetNode) {
        if (path.isEmpty()) return;
        items.put(id, new InTransitItem(id, path, 0, 0.0f, stack.copy(), networkId, targetNode));
    }

    public List<InTransitItem> tickAll(Level level) {
        List<UUID> completed = new ArrayList<>();
        for (var entry : items.entrySet()) {
            InTransitItem item = entry.getValue();
            item.tick();
            if (item.isComplete()) {
                completed.add(entry.getKey());
            }
        }
        List<InTransitItem> completedItems = new ArrayList<>();
        for (UUID id : completed) {
            InTransitItem item = items.remove(id);
            if (item != null) {
                completedItems.add(item);
            }
        }
        return completedItems;
    }

    public Collection<InTransitItem> allItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public InTransitItem getItem(UUID id) {
        return items.get(id);
    }

    public void removeItem(UUID id) {
        items.remove(id);
    }

    public void clear() {
        items.clear();
    }

    public static class InTransitItem {

        private static final float SPEED_PER_TICK = 0.02f;

        private final UUID id;
        private final List<NetworkEdge> path;
        private int currentEdgeIndex;
        private float progress;
        private final ItemStack stack;
        private final NetworkId networkId;
        private final NetworkNode targetNode;

        InTransitItem(UUID id, List<NetworkEdge> path, int currentEdgeIndex, float progress,
                      ItemStack stack, NetworkId networkId, NetworkNode targetNode) {
            this.id = id;
            this.path = List.copyOf(path);
            this.currentEdgeIndex = currentEdgeIndex;
            this.progress = progress;
            this.stack = stack;
            this.networkId = networkId;
            this.targetNode = targetNode;
        }

        void tick() {
            if (isComplete()) return;
            progress += SPEED_PER_TICK;
            if (progress >= 1.0f) {
                progress = 0.0f;
                currentEdgeIndex++;
            }
        }

        public boolean isComplete() {
            return currentEdgeIndex >= path.size();
        }

        public UUID id() {
            return id;
        }

        public NetworkEdge currentEdge() {
            return currentEdgeIndex < path.size() ? path.get(currentEdgeIndex) : null;
        }

        public float progress() {
            return progress;
        }

        public ItemStack stack() {
            return stack;
        }

        public NetworkId networkId() {
            return networkId;
        }

        public NetworkNode targetNode() {
            return targetNode;
        }

        public UUID edgeKey() {
            NetworkEdge edge = currentEdge();
            if (edge != null) {
                return UUID.nameUUIDFromBytes(edge.toString().getBytes());
            }
            return id;
        }
    }
}
