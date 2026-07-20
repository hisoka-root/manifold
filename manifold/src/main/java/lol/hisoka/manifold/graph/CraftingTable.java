package lol.hisoka.manifold.graph;

import lol.hisoka.manifold.api.crafting.CraftingRecipe;
import lol.hisoka.manifold.api.network.NetworkId;

import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CraftingTable {

    private final Map<NetworkId, List<CraftingRecipe>> tables = new ConcurrentHashMap<>();

    public boolean register(NetworkId networkId, CraftingRecipe recipe) {
        List<CraftingRecipe> recipes = tables.computeIfAbsent(networkId, k -> new ArrayList<>());
        if (detectsCycle(networkId, recipe)) {
            return false;
        }
        recipes.add(recipe);
        return true;
    }

    public boolean unregister(NetworkId networkId, CraftingRecipe recipe) {
        List<CraftingRecipe> recipes = tables.get(networkId);
        if (recipes == null) return false;
        return recipes.remove(recipe);
    }

    public List<CraftingRecipe> getRecipesFor(NetworkId networkId, ItemStack output) {
        List<CraftingRecipe> recipes = tables.get(networkId);
        if (recipes == null) return List.of();
        return recipes.stream()
                .filter(r -> ItemStack.isSameItemSameComponents(r.output(), output))
                .toList();
    }

    public List<CraftingRecipe> getAllRecipes(NetworkId networkId) {
        List<CraftingRecipe> recipes = tables.get(networkId);
        if (recipes == null) return List.of();
        return List.copyOf(recipes);
    }

    public boolean detectsCycle(NetworkId networkId, CraftingRecipe candidate) {
        Set<ItemStack> visited = new HashSet<>();
        return dfsCycleCheck(networkId, candidate, visited);
    }

    private boolean dfsCycleCheck(NetworkId networkId, CraftingRecipe current, Set<ItemStack> visited) {
        ItemStack output = current.output();
        if (!visited.add(output)) {
            return true;
        }
        List<CraftingRecipe> recipes = tables.get(networkId);
        if (recipes == null) return false;
        for (CraftingRecipe existing : recipes) {
            for (var ingredient : existing.inputs()) {
                if (ingredient.test(output)) {
                    if (dfsCycleCheck(networkId, existing, new HashSet<>(visited))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void clear(NetworkId networkId) {
        tables.remove(networkId);
    }

    public void clearAll() {
        tables.clear();
    }

    public int recipeCount(NetworkId networkId) {
        List<CraftingRecipe> recipes = tables.get(networkId);
        return recipes != null ? recipes.size() : 0;
    }
}
