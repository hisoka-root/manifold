package lol.hisoka.manifold.api.network;

import net.minecraft.world.item.ItemStack;

public record ItemAdvertisement(ItemStack stack, int count, EndpointId source) {

    public ItemAdvertisement {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative, got " + count);
        }
    }

    public ItemAdvertisement withCount(int newCount) {
        return new ItemAdvertisement(stack, newCount, source);
    }
}
