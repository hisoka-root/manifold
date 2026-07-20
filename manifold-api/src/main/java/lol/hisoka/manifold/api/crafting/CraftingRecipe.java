package lol.hisoka.manifold.api.crafting;

import lol.hisoka.manifold.api.network.EndpointId;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record CraftingRecipe(
        ItemStack output,
        List<Ingredient> inputs,
        EndpointId crafterId,
        int craftTimeTicks
) {

    public CraftingRecipe {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("CraftingRecipe must have at least one input");
        }
        if (craftTimeTicks < 0) {
            throw new IllegalArgumentException("Craft time must be non-negative");
        }
    }
}
