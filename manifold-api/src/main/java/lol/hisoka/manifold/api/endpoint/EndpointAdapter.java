package lol.hisoka.manifold.api.endpoint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface EndpointAdapter {

    int priority();

    @NotNull
    Optional<Endpoint> tryCreate(@NotNull Level level, @NotNull BlockPos pos, @NotNull Direction side);

    @Nullable
    default Object tryGetItemHandler(@NotNull Level level, @NotNull BlockPos pos, @NotNull Direction side) {
        return null;
    }

    int PRIORITY_LOWEST = Integer.MAX_VALUE;
    int PRIORITY_CAPABILITY = 1000;
    int PRIORITY_MOD_SPECIFIC = 100;
    int PRIORITY_OVERRIDE = 10;
}
