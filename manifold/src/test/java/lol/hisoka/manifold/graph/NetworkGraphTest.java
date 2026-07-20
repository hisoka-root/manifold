package lol.hisoka.manifold.graph;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkGraphTest {

    private NetworkGraph graph;
    private NetworkId networkA;

    @BeforeEach
    void setUp() {
        graph = new NetworkGraph();
        networkA = NetworkId.create();
    }

    @Test
    void addAndRetrieveNodes() {
        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        graph.addNode(j1, networkA);

        assertEquals(1, graph.nodeCount(networkA));
        assertTrue(graph.getNode("j1").isPresent());
        assertTrue(graph.getNetworkFor(j1).isPresent());
        assertEquals(networkA, graph.getNetworkFor(j1).get());
    }

    @Test
    void addAndRetrieveEdges() {
        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        NetworkNode.Junction j2 = new NetworkNode.Junction("j2");
        graph.addNode(j1, networkA);
        graph.addNode(j2, networkA);

        NetworkEdge edge = new NetworkEdge(j1, j2, 1);
        graph.addEdge(edge, networkA);

        assertEquals(1, graph.edgeCount(networkA));
        assertTrue(graph.getEdges(networkA).contains(edge));
    }

    @Test
    void edgeIsUndirected() {
        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        NetworkNode.Junction j2 = new NetworkNode.Junction("j2");
        NetworkEdge edge = new NetworkEdge(j1, j2, 1);
        NetworkEdge reverse = new NetworkEdge(j2, j1, 1);

        assertEquals(edge, reverse);
        assertEquals(edge.hashCode(), reverse.hashCode());
    }

    @Test
    void removeNodeClearsEdges() {
        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        NetworkNode.Junction j2 = new NetworkNode.Junction("j2");
        graph.addNode(j1, networkA);
        graph.addNode(j2, networkA);
        graph.addEdge(new NetworkEdge(j1, j2, 1), networkA);

        graph.removeNode(j1);

        assertFalse(graph.getNode("j1").isPresent());
        assertEquals(1, graph.nodeCount(networkA));
        assertEquals(0, graph.edgeCount(networkA));
    }

    @Test
    void mergeTwoNetworks() {
        NetworkId networkB = NetworkId.create();
        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        NetworkNode.Junction j2 = new NetworkNode.Junction("j2");

        graph.addNode(j1, networkA);
        graph.addNode(j2, networkB);

        graph.mergeNetworks(networkB, networkA);

        assertFalse(graph.allNetworkIds().contains(networkB));
        assertEquals(2, graph.nodeCount(networkA));
        assertTrue(graph.getNode("j2").isPresent());
        assertEquals(networkA, graph.getNetworkFor(j2).get());
    }

    @Test
    void splitNetworkOnNodeRemoval() {
        NetworkNode.Junction j1 = new NetworkNode.Junction("j1");
        NetworkNode.Junction j2 = new NetworkNode.Junction("j2");
        NetworkNode.Junction j3 = new NetworkNode.Junction("bridge");

        graph.addNode(j1, networkA);
        graph.addNode(j2, networkA);
        graph.addNode(j3, networkA);
        graph.addEdge(new NetworkEdge(j1, j3, 1), networkA);
        graph.addEdge(new NetworkEdge(j2, j3, 1), networkA);

        List<NetworkGraph.NetworkSplitResult> results = graph.splitNetwork(networkA, j3);

        assertFalse(results.isEmpty(), "Should produce at least one new network");
        assertEquals(1, results.size(), "Should produce exactly one new network");
    }

    @Test
    void clearGraph() {
        graph.addNode(new NetworkNode.Junction("j1"), networkA);
        graph.clear();

        assertEquals(0, graph.allNetworkIds().size());
        assertEquals(0, graph.nodeCount(networkA));
    }
}
