package lol.hisoka.manifold.capability;

import lol.hisoka.manifold.api.endpoint.Endpoint;
import lol.hisoka.manifold.api.endpoint.EndpointType;
import lol.hisoka.manifold.api.network.EndpointId;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CapabilityEndpoint implements Endpoint {

    private final EndpointId id;
    private final EndpointType type;
    private final Direction facing;
    private final BlockCapabilityCache<IItemHandler, Direction> handlerCache;

    CapabilityEndpoint(EndpointId id, EndpointType type, Direction facing,
                       @Nullable BlockCapabilityCache<IItemHandler, Direction> handlerCache) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.facing = Objects.requireNonNull(facing);
        this.handlerCache = handlerCache;
    }

    @Override
    public EndpointId id() {
        return id;
    }

    @Override
    public EndpointType type() {
        return type;
    }

    @Override
    public Direction facing() {
        return facing;
    }

    @Nullable
    public IItemHandler getItemHandler() {
        return handlerCache != null ? handlerCache.getCapability() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapabilityEndpoint that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "CapabilityEndpoint[%s, type=%s, facing=%s]".formatted(id, type, facing);
    }
}
