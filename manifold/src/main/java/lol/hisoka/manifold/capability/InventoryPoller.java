package lol.hisoka.manifold.capability;

import lol.hisoka.manifold.api.network.EndpointId;
import lol.hisoka.manifold.api.network.ItemAdvertisement;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryPoller {

    private final Map<EndpointId, AdvertisementCache> caches = new ConcurrentHashMap<>();

    public PollResult poll(EndpointId endpointId, IItemHandler handler, long currentTick) {
        List<ItemAdvertisement> advertisements = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                advertisements.add(new ItemAdvertisement(
                        stack.copyWithCount(1),
                        stack.getCount(),
                        endpointId
                ));
            }
        }

        AdvertisementCache cache = caches.computeIfAbsent(endpointId, k -> new AdvertisementCache());
        boolean changed = !cache.sameAs(advertisements);
        if (changed) {
            cache.update(advertisements);
        }

        return new PollResult(List.copyOf(advertisements), changed);
    }

    @Nullable
    public List<ItemAdvertisement> getCached(EndpointId endpointId) {
        AdvertisementCache cache = caches.get(endpointId);
        return cache != null ? List.copyOf(cache.advertisements) : null;
    }

    public void removeEndpoint(EndpointId endpointId) {
        caches.remove(endpointId);
    }

    public void clear(EndpointId endpointId) {
        caches.remove(endpointId);
    }

    public void clearAll() {
        caches.clear();
    }

    public record PollResult(List<ItemAdvertisement> advertisements, boolean changed) {}

    private static final class AdvertisementCache {
        List<ItemAdvertisement> advertisements = List.of();

        boolean sameAs(List<ItemAdvertisement> other) {
            if (advertisements.size() != other.size()) return false;
            for (int i = 0; i < advertisements.size(); i++) {
                ItemAdvertisement a = advertisements.get(i);
                ItemAdvertisement b = other.get(i);
                if (!ItemStack.isSameItemSameComponents(a.stack(), b.stack())
                        || a.count() != b.count()) {
                    return false;
                }
            }
            return true;
        }

        void update(List<ItemAdvertisement> ads) {
            this.advertisements = List.copyOf(ads);
        }
    }
}
