package lol.hisoka.manifold.api.network;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class EndpointId {

    private final BlockPos pos;
    private final UUID nonce;

    public EndpointId(@NotNull BlockPos pos, @NotNull UUID nonce) {
        this.pos = pos.immutable();
        this.nonce = Objects.requireNonNull(nonce);
    }

    @NotNull
    public BlockPos pos() {
        return pos;
    }

    public UUID nonce() {
        return nonce;
    }

    public static EndpointId create(BlockPos pos) {
        return new EndpointId(pos, UUID.randomUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointId that)) return false;
        return pos.equals(that.pos) && nonce.equals(that.nonce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, nonce);
    }

    @Override
    public String toString() {
        return "EndpointId[pos=%s, nonce=%s]".formatted(pos, nonce);
    }
}
