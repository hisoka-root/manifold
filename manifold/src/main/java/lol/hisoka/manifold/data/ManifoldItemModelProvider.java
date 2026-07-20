package lol.hisoka.manifold.data;

import lol.hisoka.manifold.Manifold;
import lol.hisoka.manifold.block.ManifoldRegistries;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.function.Supplier;

public final class ManifoldItemModelProvider extends ItemModelProvider {

    private static final Map<Supplier<? extends Item>, String> ITEMS = Map.of(
            ManifoldRegistries.TRANSPORT_PIPE_ITEM, "transport",
            ManifoldRegistries.PROVIDER_PIPE_ITEM,  "provider",
            ManifoldRegistries.REQUESTER_PIPE_ITEM, "requester",
            ManifoldRegistries.SUPPLIER_PIPE_ITEM,  "supplier"
    );

    public ManifoldItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Manifold.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var entry : ITEMS.entrySet()) {
            withExistingParent(entry.getKey().get().builtInRegistryHolder().key().location().getPath(),
                    modLoc("block/pipe_core_" + entry.getValue()));
        }

        withExistingParent("wrench", mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/wrench"));
    }
}
