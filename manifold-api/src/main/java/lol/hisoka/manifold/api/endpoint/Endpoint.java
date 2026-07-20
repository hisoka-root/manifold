package lol.hisoka.manifold.api.endpoint;

import lol.hisoka.manifold.api.network.EndpointId;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public interface Endpoint {

    @NotNull EndpointId id();
    @NotNull EndpointType type();
    @NotNull Direction facing();

    default boolean isProvider() {
        return type().isProvider();
    }

    default boolean isRequester() {
        return type().isRequester();
    }

    default boolean isSupplier() {
        return type().isSupplier();
    }

    default boolean isCrafting() {
        return type() == EndpointType.CRAFTING;
    }

    default boolean isChassis() {
        return type() == EndpointType.CHASSIS;
    }
}
