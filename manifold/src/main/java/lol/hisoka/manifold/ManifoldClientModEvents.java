package lol.hisoka.manifold;

import lol.hisoka.manifold.block.ManifoldRegistries;
import lol.hisoka.manifold.rendering.PipeBlockEntityRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Manifold.MOD_ID, value = Dist.CLIENT)
public final class ManifoldClientModEvents {

    private ManifoldClientModEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ManifoldRegistries.PIPE_BLOCK_ENTITY.get(),
                PipeBlockEntityRenderer::new
        );
    }
}
