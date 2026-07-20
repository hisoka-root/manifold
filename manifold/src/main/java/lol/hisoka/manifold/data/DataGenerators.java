package lol.hisoka.manifold.data;

import lol.hisoka.manifold.Manifold;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Manifold.MOD_ID)
public final class DataGenerators {

    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ManifoldBlockModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ManifoldBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ManifoldItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ManifoldLanguageProvider(output, "en_us"));

        generator.addProvider(event.includeServer(), new ManifoldRecipeProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ManifoldLootTableProvider(output, event.getLookupProvider()));
    }
}
