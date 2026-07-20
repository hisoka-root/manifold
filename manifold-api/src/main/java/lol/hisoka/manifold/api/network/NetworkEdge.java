package lol.hisoka.manifold.api.network;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NetworkEdge {

    private final NetworkNode source;
    private final NetworkNode target;
    private final int weight;

    public NetworkEdge(NetworkNode source, NetworkNode target, int weight) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.weight = Math.max(1, weight);
    }

    public NetworkNode source() {
        return source;
    }

    public NetworkNode target() {
        return target;
    }

    public int weight() {
        return weight;
    }

    public boolean connects(NetworkNode a, NetworkNode b) {
        return (source.equals(a) && target.equals(b))
                || (source.equals(b) && target.equals(a));
    }

    public NetworkNode other(NetworkNode node) {
        if (source.equals(node)) return target;
        if (target.equals(node)) return source;
        throw new IllegalArgumentException("Node %s is not an endpoint of this edge".formatted(node));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkEdge that)) return false;
        return (source.equals(that.source) && target.equals(that.target))
                || (source.equals(that.target) && target.equals(that.source));
    }

    @Override
    public int hashCode() {
        int h1 = source.hashCode();
        int h2 = target.hashCode();
        return h1 < h2 ? Objects.hash(h1, h2) : Objects.hash(h2, h1);
    }

    @Override
    public String toString() {
        return "NetworkEdge[%s <-> %s, w=%d]".formatted(source, target, weight);
    }
}
