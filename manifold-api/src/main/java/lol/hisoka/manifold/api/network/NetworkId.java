package lol.hisoka.manifold.api.network;

import java.util.UUID;

public record NetworkId(UUID id) {

    public static NetworkId create() {
        return new NetworkId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
