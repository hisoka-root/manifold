package lol.hisoka.manifold.rendering;

import lol.hisoka.manifold.network.ClientItemTracker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;

public final class InPipeItemRenderer {

    private static final InPipeItemRenderer INSTANCE = new InPipeItemRenderer();

    public static InPipeItemRenderer get() {
        return INSTANCE;
    }

    public void renderItems(BlockPos chunkOrigin, PoseStack poseStack,
                             MultiBufferSource buffer, float partialTick,
                             int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ItemRenderer itemRenderer = mc.getItemRenderer();

        for (ClientItemTracker.TrackedItem tracked : ClientItemTracker.get().allItems()) {
            BlockPos mid = midChunk(tracked);
            if (!mid.equals(chunkOrigin)) continue;

            double x = lerp(tracked.startPos().getX(), tracked.endPos().getX(), tracked.progress());
            double y = lerp(tracked.startPos().getY(), tracked.endPos().getY(), tracked.progress());
            double z = lerp(tracked.startPos().getZ(), tracked.endPos().getZ(), tracked.progress());

            double cameraX = mc.gameRenderer.getMainCamera().getPosition().x;
            double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;
            double cameraZ = mc.gameRenderer.getMainCamera().getPosition().z;

            poseStack.pushPose();
            poseStack.translate(x - cameraX, y - cameraY, z - cameraZ);
            poseStack.scale(0.4f, 0.4f, 0.4f);

            itemRenderer.renderStatic(
                    tracked.item(),
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    mc.level,
                    (int) tracked.transitId().getLeastSignificantBits()
            );

            poseStack.popPose();
        }
    }

    private BlockPos midChunk(ClientItemTracker.TrackedItem item) {
        double mx = (item.startPos().getX() + item.endPos().getX()) / 2.0;
        double my = (item.startPos().getY() + item.endPos().getY()) / 2.0;
        double mz = (item.startPos().getZ() + item.endPos().getZ()) / 2.0;
        return new BlockPos((int) Math.floor(mx / 16.0) * 16,
                (int) Math.floor(my / 16.0) * 16,
                (int) Math.floor(mz / 16.0) * 16);
    }

    private double lerp(int a, int b, float t) {
        return a + (b - a + 0.5) * t + 0.5;
    }

    private InPipeItemRenderer() {}
}
