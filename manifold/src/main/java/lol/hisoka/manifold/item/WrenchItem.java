package lol.hisoka.manifold.item;

import lol.hisoka.manifold.block.AbstractPipeBlock;
import lol.hisoka.manifold.blockentity.PipeBlockEntity;
import lol.hisoka.manifold.api.endpoint.EndpointType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (!(state.getBlock() instanceof AbstractPipeBlock pipeBlock)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player != null && player.isShiftKeyDown()) {
            PipeBlockEntity pipe = (PipeBlockEntity) level.getBlockEntity(pos);
            if (pipe != null) {
                EndpointType type = pipe.getPipeType();
                String info = switch (type) {
                    case REQUESTER -> "Filter: %s".formatted(
                            pipe.getFilter().isEmpty() ? "empty" : pipe.getFilter().size() + " items");
                    case SUPPLIER -> "Buffer targets: %d".formatted(pipe.getBufferTargets().size());
                    case PROVIDER -> "Connected sides: %s".formatted(pipe.connectedSides());
                    default -> "Type: " + type.name();
                };
                player.displayClientMessage(Component.literal(info), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (player != null) {
            level.destroyBlock(pos, true, player);
        }

        return InteractionResult.SUCCESS;
    }
}
