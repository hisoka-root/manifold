package lol.hisoka.manifold.routing;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;
import lol.hisoka.manifold.graph.NetworkGraph;

import java.util.*;

public class DijkstraPathResolver {

    private final NetworkGraph graph;

    public DijkstraPathResolver(NetworkGraph graph) {
        this.graph = graph;
    }

    public Map<NetworkNode, List<NetworkEdge>> resolvePaths(NetworkId networkId,
                                                             NetworkNode source) {
        Set<NetworkNode> nodes = graph.getNodes(networkId);
        Set<NetworkEdge> edges = graph.getEdges(networkId);

        Map<NetworkNode, Integer> distance = new HashMap<>();
        Map<NetworkNode, NetworkNode> previous = new HashMap<>();
        Map<NetworkNode, NetworkEdge> incomingEdge = new HashMap<>();
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>();

        for (NetworkNode node : nodes) {
            distance.put(node, Integer.MAX_VALUE);
        }
        distance.put(source, 0);
        queue.add(new NodeEntry(source, 0));

        while (!queue.isEmpty()) {
            NodeEntry entry = queue.poll();
            NetworkNode current = entry.node;
            int currentDist = distance.getOrDefault(current, Integer.MAX_VALUE);
            if (entry.distance > currentDist) {
                continue;
            }
            if (currentDist == Integer.MAX_VALUE) {
                continue;
            }

            for (NetworkEdge edge : edges) {
                NetworkNode neighbor = null;
                if (edge.source().equals(current)) neighbor = edge.target();
                else if (edge.target().equals(current)) neighbor = edge.source();
                if (neighbor == null) continue;

                int newDist = currentDist + edge.weight();
                if (newDist < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distance.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    incomingEdge.put(neighbor, edge);
                    queue.add(new NodeEntry(neighbor, newDist));
                }
            }
        }

        Map<NetworkNode, List<NetworkEdge>> paths = new LinkedHashMap<>();
        for (NetworkNode node : nodes) {
            if (node.equals(source) || !node.isEndpoint()) continue;
            List<NetworkEdge> path = reconstructPath(node, previous, incomingEdge);
            if (!path.isEmpty()) {
                paths.put(node, path);
            }
        }

        return paths;
    }

    private List<NetworkEdge> reconstructPath(NetworkNode target,
                                               Map<NetworkNode, NetworkNode> previous,
                                               Map<NetworkNode, NetworkEdge> incomingEdge) {
        LinkedList<NetworkEdge> path = new LinkedList<>();
        NetworkNode current = target;
        while (previous.containsKey(current)) {
            NetworkEdge edge = incomingEdge.get(current);
            if (edge != null) {
                path.addFirst(edge);
            }
            current = previous.get(current);
            if (current == null) break;
        }
        return path;
    }

    private record NodeEntry(NetworkNode node, int distance) implements Comparable<NodeEntry> {
        @Override
        public int compareTo(NodeEntry other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
}
