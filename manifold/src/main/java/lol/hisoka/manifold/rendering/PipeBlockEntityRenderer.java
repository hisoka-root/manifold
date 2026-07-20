package lol.hisoka.manifold.rendering;

import lol.hisoka.manifold.block.AbstractPipeBlock;
import lol.hisoka.manifold.blockentity.PipeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity> {

    public PipeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PipeBlockEntity pipe, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = pipe.getBlockState();
        if (!(state.getBlock() instanceof AbstractPipeBlock)) return;

        boolean needsUpdate = false;
        for (Direction side : Direction.values()) {
            boolean shouldConnect = pipe.isConnected(side);
            boolean isConnected = state.getValue(AbstractPipeBlock.propertyFor(side));
            if (shouldConnect != isConnected) {
                needsUpdate = true;
                break;
            }
        }

        if (needsUpdate && pipe.getLevel() != null) {
            BlockState newState = state;
            for (Direction side : Direction.values()) {
                newState = newState.setValue(AbstractPipeBlock.propertyFor(side), pipe.isConnected(side));
            }
            pipe.getLevel().setBlock(pipe.getBlockPos(), newState, 3);
        }
    }
}
