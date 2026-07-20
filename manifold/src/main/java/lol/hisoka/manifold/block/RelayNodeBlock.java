package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;
import lol.hisoka.manifold.blockentity.PipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

public final class RelayNodeBlock extends AbstractPipeBlock {

    public RelayNodeBlock(Properties properties) {
        super(properties, EndpointType.TRANSPORT);
    }
}
