package lol.hisoka.manifold.api.network;

import lol.hisoka.manifold.api.endpoint.Endpoint;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed interface NetworkNode permits NetworkNode.Junction, NetworkNode.EndpointWrapper {

    default boolean isEndpoint() {
        return this instanceof EndpointWrapper;
    }

    default boolean isJunction() {
        return this instanceof Junction;
    }

    @NotNull String nodeId();

    record Junction(@NotNull String nodeId) implements NetworkNode {

        public Junction {
            Objects.requireNonNull(nodeId);
        }
    }

    record EndpointWrapper(@NotNull Endpoint endpoint) implements NetworkNode {

        public EndpointWrapper {
            Objects.requireNonNull(endpoint);
        }

        @Override
        public String nodeId() {
            return endpoint.id().toString();
        }
    }
}
