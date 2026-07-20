package lol.hisoka.manifold.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientItemTracker {

    private static final ClientItemTracker INSTANCE = new ClientItemTracker();

    private final Map<UUID, TrackedItem> items = new ConcurrentHashMap<>();

    public static ClientItemTracker get() {
        return INSTANCE;
    }

    public void updatePosition(UUID transitId, BlockPos start, BlockPos end,
                                float progress, ItemStack item) {
        items.put(transitId, new TrackedItem(transitId, start, end, progress, item));
    }

    public void removeItem(UUID transitId) {
        items.remove(transitId);
    }

    public Collection<TrackedItem> allItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public void clear() {
        items.clear();
    }

    public record TrackedItem(
            UUID transitId,
            BlockPos startPos,
            BlockPos endPos,
            float progress,
            ItemStack item
    ) {}
}
