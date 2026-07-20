package lol.hisoka.manifold;

import lol.hisoka.manifold.rendering.InPipeItemRenderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Manifold.MOD_ID, value = Dist.CLIENT)
public final class ManifoldClient {

    private ManifoldClient() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        BlockPos playerChunk = mc.player.chunkPosition().getWorldPosition();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                InPipeItemRenderer.get().renderItems(
                        playerChunk.offset(dx * 16, 0, dz * 16),
                        poseStack, buffer, event.getPartialTick().getGameTimeDeltaTicks(),
                        0xF000F0
                );
            }
        }

        buffer.endBatch();
    }
}
