package lol.hisoka.manifold.graph;

import lol.hisoka.manifold.api.crafting.CraftingRecipe;
import lol.hisoka.manifold.api.network.EndpointId;
import lol.hisoka.manifold.api.network.NetworkId;

import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.ToIntFunction;

public class CraftingResolver {

    private final CraftingTable table;
    private final ToIntFunction<ItemStack> stockLookup;

    public CraftingResolver(CraftingTable table, ToIntFunction<ItemStack> stockLookup) {
        this.table = table;
        this.stockLookup = stockLookup;
    }

    public Optional<CraftingJob> resolve(NetworkId networkId, ItemStack requested, int needed) {
        int available = stockLookup.applyAsInt(requested);
        if (available >= needed) {
            return Optional.empty();
        }

        int deficit = needed - available;
        List<CraftingRecipe> recipes = table.getRecipesFor(networkId, requested);

        if (recipes.isEmpty()) {
            return Optional.empty();
        }

        CraftingRecipe bestRecipe = recipes.getFirst();

        List<CraftingJob> dependencies = new ArrayList<>();
        for (var ingredient : bestRecipe.inputs()) {
            for (ItemStack matching : ingredient.getItems()) {
                CraftingJob subJob = resolveIngredient(networkId, matching, ingredient.getItems().length);
                if (subJob != null) {
                    dependencies.add(subJob);
                }
            }
        }

        CraftingJob job = new CraftingJob(bestRecipe, deficit, dependencies);
        return Optional.of(job);
    }

    private CraftingJob resolveIngredient(NetworkId networkId, ItemStack ingredient, int needed) {
        int available = stockLookup.applyAsInt(ingredient);
        if (available >= needed) {
            return null;
        }

        int deficit = needed - available;
        Optional<CraftingJob> subJob = resolve(networkId, ingredient, deficit);
        return subJob.orElse(null);
    }

    public record CraftingJob(CraftingRecipe recipe, int quantity, List<CraftingJob> dependencies) {

        public enum State { PENDING, IN_PROGRESS, BLOCKED, COMPLETE }

        public boolean isReady() {
            return dependencies.isEmpty()
                    || dependencies.stream().allMatch(d -> d.currentState() == State.COMPLETE);
        }

        public State currentState() {
            if (!isReady()) return State.BLOCKED;
            if (dependencies.stream().anyMatch(d -> d.currentState() == State.BLOCKED)) {
                return State.BLOCKED;
            }
            return State.PENDING;
        }

        public int totalSteps() {
            return 1 + dependencies.stream().mapToInt(CraftingJob::totalSteps).sum();
        }
    }
}
