package lol.hisoka.manifold.routing;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;
import lol.hisoka.manifold.graph.NetworkGraph;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.CompletableFuture;

public class RoutingTable {

    private record RouteKey(NetworkId networkId, String sourceNodeId) {}

    private final NetworkGraph graph;
    private final DijkstraPathResolver resolver;
    private final ForkJoinPool workerPool;
    private final Map<RouteKey, Map<NetworkNode, List<NetworkEdge>>> cachedRoutes = new ConcurrentHashMap<>();
    private final Map<NetworkId, Boolean> dirtyFlags = new ConcurrentHashMap<>();

    public RoutingTable(NetworkGraph graph) {
        this.graph = graph;
        this.resolver = new DijkstraPathResolver(graph);
        int threads = Math.clamp(Runtime.getRuntime().availableProcessors() / 2, 1, 4);
        this.workerPool = new ForkJoinPool(threads);
    }

    public void markDirty(NetworkId networkId) {
        dirtyFlags.put(networkId, true);
    }

    public boolean isDirty(NetworkId networkId) {
        return dirtyFlags.getOrDefault(networkId, false);
    }

    public CompletableFuture<Map<NetworkNode, List<NetworkEdge>>> recomputeAsync(NetworkId networkId,
                                                                                   NetworkNode source) {
        RouteKey key = new RouteKey(networkId, source.nodeId());
        return CompletableFuture.supplyAsync(() -> {
            Map<NetworkNode, List<NetworkEdge>> paths = resolver.resolvePaths(networkId, source);
            cachedRoutes.put(key, paths);
            dirtyFlags.put(networkId, false);
            return paths;
        }, workerPool);
    }

    public Map<NetworkNode, List<NetworkEdge>> getCached(NetworkId networkId, String sourceNodeId) {
        return cachedRoutes.getOrDefault(new RouteKey(networkId, sourceNodeId), Map.of());
    }

    public List<NetworkEdge> getPath(NetworkId networkId, NetworkNode target, NetworkNode source) {
        Map<NetworkNode, List<NetworkEdge>> routes = getCached(networkId, source.nodeId());
        return routes.getOrDefault(target, List.of());
    }

    public void invalidate(NetworkId networkId) {
        cachedRoutes.keySet().removeIf(k -> k.networkId.equals(networkId));
        dirtyFlags.remove(networkId);
    }

    public void shutdown() {
        workerPool.shutdown();
    }
}
