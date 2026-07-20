package lol.hisoka.manifold.capability;

import lol.hisoka.manifold.api.endpoint.Endpoint;
import lol.hisoka.manifold.api.endpoint.EndpointAdapter;
import lol.hisoka.manifold.api.endpoint.EndpointType;
import lol.hisoka.manifold.api.network.EndpointId;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class CapabilityEndpointAdapter implements EndpointAdapter {

    @Override
    public int priority() {
        return PRIORITY_CAPABILITY;
    }

    @Override
    public Optional<Endpoint> tryCreate(Level level, BlockPos pos, Direction side) {
        return tryCreate(level, pos, side, EndpointType.PROVIDER);
    }

    public Optional<Endpoint> tryCreate(Level level, BlockPos pos, Direction side, EndpointType type) {
        BlockCapabilityCache<IItemHandler, Direction> cache = null;
        IItemHandler handler;

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            cache = BlockCapabilityCache.create(
                    Capabilities.ItemHandler.BLOCK, serverLevel, pos, side
            );
            handler = cache.getCapability();
        } else {
            handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        }

        if (handler == null || handler.getSlots() == 0) {
            return Optional.empty();
        }

        EndpointId endpointId = EndpointId.create(pos);

        return Optional.of(new CapabilityEndpoint(
                endpointId, type, side, cache
        ));
    }

    @Nullable
    @Override
    public Object tryGetItemHandler(Level level, BlockPos pos, Direction side) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            var cache = BlockCapabilityCache.create(
                    Capabilities.ItemHandler.BLOCK, serverLevel, pos, side
            );
            return cache.getCapability();
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
    }

    public static Optional<IItemHandler> getItemHandler(Level level, BlockPos pos, Direction side) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            var cache = BlockCapabilityCache.create(
                    Capabilities.ItemHandler.BLOCK, serverLevel, pos, side
            );
            return Optional.ofNullable(cache.getCapability());
        }
        return Optional.ofNullable(level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side));
    }
}
