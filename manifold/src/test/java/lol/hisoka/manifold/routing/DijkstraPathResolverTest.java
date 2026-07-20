package lol.hisoka.manifold.routing;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;
import lol.hisoka.manifold.graph.NetworkGraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraPathResolverTest {

    private NetworkGraph graph;
    private DijkstraPathResolver resolver;
    private NetworkId networkId;
    private NetworkNode source;
    private NetworkNode target;

    @BeforeEach
    void setUp() {
        graph = new NetworkGraph();
        resolver = new DijkstraPathResolver(graph);
        networkId = NetworkId.create();

        source = new NetworkNode.Junction("source");
        target = new NetworkNode.Junction("target");

        graph.addNode(source, networkId);
        graph.addNode(target, networkId);

        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        NetworkNode.Junction j2 = new NetworkNode.Junction("j2");
        graph.addNode(j1, networkId);
        graph.addNode(j2, networkId);

        graph.addEdge(new NetworkEdge(source, j1, 1), networkId);
        graph.addEdge(new NetworkEdge(j1, j2, 1), networkId);
        graph.addEdge(new NetworkEdge(j2, target, 1), networkId);
    }

    @Test
    void findsShortestPath() {
        Map<NetworkNode, List<NetworkEdge>> paths = resolver.resolvePaths(networkId, source);

        assertTrue(paths.containsKey(target), "Should find a path to the target");
        List<NetworkEdge> path = paths.get(target);
        assertEquals(3, path.size(), "Path should have 3 edges");
    }

    @Test
    void noPathToSelf() {
        Map<NetworkNode, List<NetworkEdge>> paths = resolver.resolvePaths(networkId, source);

        assertFalse(paths.containsKey(source), "Should not include path to self");
    }

    @Test
    void prefersLowerWeightPath() {
        NetworkNode.Junction j3 = new NetworkNode.Junction("j3");
        graph.addNode(j3, networkId);

        graph.addEdge(new NetworkEdge(target, j3, 10), networkId);
        graph.addEdge(new NetworkEdge(j3, source, 10), networkId);

        Map<NetworkNode, List<NetworkEdge>> paths = resolver.resolvePaths(networkId, source);
        List<NetworkEdge> path = paths.get(target);

        int totalWeight = path.stream().mapToInt(NetworkEdge::weight).sum();
        assertEquals(3, totalWeight, "Should prefer the 3-weight path over the 20-weight alternative");
    }

    @Test
    void disconnectedNodeHasNoPath() {
        NetworkNode.Junction isolated = new NetworkNode.Junction("isolated");
        graph.addNode(isolated, networkId);

        Map<NetworkNode, List<NetworkEdge>> paths = resolver.resolvePaths(networkId, source);

        assertFalse(paths.containsKey(isolated), "Isolated node should have no path");
    }

    @Test
    void pathWeightsMatchEdgeList() {
        Map<NetworkNode, List<NetworkEdge>> paths = resolver.resolvePaths(networkId, source);
        List<NetworkEdge> path = paths.get(target);

        assertNotNull(path);
        assertTrue(path.stream().allMatch(e -> e.weight() == 1));
    }
}
