package lol.hisoka.manifold.data;

import lol.hisoka.manifold.Manifold;
import lol.hisoka.manifold.block.ManifoldRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public final class ManifoldRecipeProvider extends RecipeProvider {

    public ManifoldRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ManifoldRegistries.TRANSPORT_PIPE_ITEM.get())
                .pattern("IGI")
                .pattern("G G")
                .pattern("IGI")
                .define('I', Items.IRON_INGOT)
                .define('G', ItemTags.create(ResourceLocation.withDefaultNamespace("glass_panes")))
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ManifoldRegistries.PROVIDER_PIPE_ITEM.get())
                .pattern("IGI")
                .pattern("G G")
                .pattern("ICI")
                .define('I', Items.IRON_INGOT)
                .define('G', ItemTags.create(ResourceLocation.withDefaultNamespace("glass_panes")))
                .define('C', Items.CHEST)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ManifoldRegistries.REQUESTER_PIPE_ITEM.get())
                .pattern("IGI")
                .pattern("G G")
                .pattern("IGI")
                .define('I', Items.GOLD_INGOT)
                .define('G', ItemTags.create(ResourceLocation.withDefaultNamespace("glass_panes")))
                .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ManifoldRegistries.SUPPLIER_PIPE_ITEM.get())
                .pattern("IGI")
                .pattern("G G")
                .pattern("IDI")
                .define('I', Items.IRON_INGOT)
                .define('G', ItemTags.create(ResourceLocation.withDefaultNamespace("glass_panes")))
                .define('D', Items.DISPENSER)
                .unlockedBy("has_dispenser", has(Items.DISPENSER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ManifoldRegistries.WRENCH.get())
                .pattern("I I")
                .pattern(" G ")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('G', ItemTags.create(ResourceLocation.withDefaultNamespace("glass_panes")))
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);
    }
}
