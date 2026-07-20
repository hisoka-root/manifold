package lol.hisoka.manifold.graph;

import lol.hisoka.manifold.api.crafting.CraftingRecipe;
import lol.hisoka.manifold.api.network.EndpointId;
import lol.hisoka.manifold.api.network.NetworkId;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CraftingTableTest {

    private CraftingTable table;
    private NetworkId networkId;
    private EndpointId crafter;

    @BeforeEach
    void setUp() {
        table = new CraftingTable();
        networkId = NetworkId.create();
        crafter = EndpointId.create(BlockPos.ZERO);
    }

    @Test
    void registerAndRetrieveRecipe() {
        ItemStack output = new ItemStack(Items.DIAMOND);
        CraftingRecipe recipe = new CraftingRecipe(
                output,
                List.of(Ingredient.of(Items.COAL)),
                crafter,
                20
        );

        assertTrue(table.register(networkId, recipe));
        assertEquals(1, table.recipeCount(networkId));
        assertEquals(1, table.getRecipesFor(networkId, output).size());
    }

    @Test
    void rejectsDirectCycle() {
        ItemStack output = new ItemStack(Items.DIAMOND);
        CraftingRecipe recipe = new CraftingRecipe(
                output,
                List.of(Ingredient.of(Items.OAK_PLANKS)),
                crafter,
                20
        );
        assertTrue(table.register(networkId, recipe));

        CraftingRecipe cyclic = new CraftingRecipe(
                new ItemStack(Items.OAK_PLANKS),
                List.of(Ingredient.of(Items.DIAMOND)),
                crafter,
                20
        );

        assertFalse(table.register(networkId, cyclic), "Should reject cyclic recipe");
        assertEquals(1, table.recipeCount(networkId));
    }

    @Test
    void unregisterRecipe() {
        CraftingRecipe recipe = new CraftingRecipe(
                new ItemStack(Items.IRON_INGOT),
                List.of(Ingredient.of(Items.IRON_ORE)),
                crafter,
                20
        );

        assertTrue(table.register(networkId, recipe));
        assertTrue(table.unregister(networkId, recipe));
        assertEquals(0, table.recipeCount(networkId));
    }

    @Test
    void clearNetwork() {
        table.register(networkId, new CraftingRecipe(
                new ItemStack(Items.GOLD_INGOT),
                List.of(Ingredient.of(Items.GOLD_ORE)),
                crafter,
                20
        ));

        table.clear(networkId);
        assertEquals(0, table.recipeCount(networkId));
    }
}
