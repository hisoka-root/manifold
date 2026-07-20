package lol.hisoka.manifold.data;

import lol.hisoka.manifold.block.ManifoldRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ManifoldLootTableProvider extends LootTableProvider {

    public ManifoldLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ManifoldBlockLoot::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    private static final class ManifoldBlockLoot extends BlockLootSubProvider {

        ManifoldBlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(ManifoldRegistries.TRANSPORT_PIPE.get());
            dropSelf(ManifoldRegistries.PROVIDER_PIPE.get());
            dropSelf(ManifoldRegistries.REQUESTER_PIPE.get());
            dropSelf(ManifoldRegistries.SUPPLIER_PIPE.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    ManifoldRegistries.TRANSPORT_PIPE.get(),
                    ManifoldRegistries.PROVIDER_PIPE.get(),
                    ManifoldRegistries.REQUESTER_PIPE.get(),
                    ManifoldRegistries.SUPPLIER_PIPE.get()
            );
        }
    }
}
