package lol.hisoka.manifold.graph;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkGraph {

    private final Map<NetworkId, Map<String, NetworkNode>> nodesByNetwork = new HashMap<>();
    private final Map<NetworkId, Set<NetworkEdge>> edgesByNetwork = new HashMap<>();
    private final Map<String, NetworkNode> allNodes = new HashMap<>();
    private final Map<String, NetworkId> nodeToNetwork = new HashMap<>();

    public void addNode(NetworkNode node, NetworkId networkId) {
        String id = node.nodeId();
        NetworkId oldNetwork = nodeToNetwork.get(id);
        if (oldNetwork != null && !oldNetwork.equals(networkId)) {
            Map<String, NetworkNode> oldNodes = nodesByNetwork.get(oldNetwork);
            if (oldNodes != null) {
                oldNodes.remove(id);
                if (oldNodes.isEmpty()) {
                    nodesByNetwork.remove(oldNetwork);
                }
            }
        }
        allNodes.put(id, node);
        nodeToNetwork.put(id, networkId);
        nodesByNetwork.computeIfAbsent(networkId, k -> new HashMap<>())
                .put(id, node);
    }

    public void removeNode(NetworkNode node) {
        String id = node.nodeId();
        NetworkId networkId = nodeToNetwork.remove(id);
        allNodes.remove(id);
        if (networkId != null) {
            Map<String, NetworkNode> nodes = nodesByNetwork.get(networkId);
            if (nodes != null) {
                nodes.remove(id);
                if (nodes.isEmpty()) {
                    nodesByNetwork.remove(networkId);
                }
            }
            Set<NetworkEdge> edges = edgesByNetwork.get(networkId);
            if (edges != null) {
                edges.removeIf(e -> e.source().equals(node) || e.target().equals(node));
                if (edges.isEmpty()) {
                    edgesByNetwork.remove(networkId);
                }
            }
        }
    }

    public void addEdge(NetworkEdge edge, NetworkId networkId) {
        edgesByNetwork.computeIfAbsent(networkId, k -> new HashSet<>()).add(edge);
    }

    public void removeEdge(NetworkEdge edge, NetworkId networkId) {
        Set<NetworkEdge> edges = edgesByNetwork.get(networkId);
        if (edges != null) {
            edges.remove(edge);
            if (edges.isEmpty()) {
                edgesByNetwork.remove(networkId);
            }
        }
    }

    public void mergeNetworks(NetworkId from, NetworkId into) {
        if (from.equals(into)) return;

        Map<String, NetworkNode> fromNodes = nodesByNetwork.remove(from);
        Set<NetworkEdge> fromEdges = edgesByNetwork.remove(from);

        if (fromNodes != null) {
            nodesByNetwork.computeIfAbsent(into, k -> new HashMap<>()).putAll(fromNodes);
            for (String nodeId : fromNodes.keySet()) {
                nodeToNetwork.put(nodeId, into);
            }
        }
        if (fromEdges != null) {
            edgesByNetwork.computeIfAbsent(into, k -> new HashSet<>()).addAll(fromEdges);
        }
    }

    public List<NetworkSplitResult> splitNetwork(NetworkId original, NetworkNode removedNode) {
        Map<String, NetworkNode> nodes = nodesByNetwork.get(original);
        Set<NetworkEdge> edges = edgesByNetwork.get(original);
        if (nodes == null || edges == null) return List.of();

        Set<String> remaining = new HashSet<>(nodes.keySet());
        remaining.remove(removedNode.nodeId());

        if (remaining.isEmpty()) {
            nodesByNetwork.remove(original);
            edgesByNetwork.remove(original);
            return List.of();
        }

        Map<String, Set<String>> adjacency = buildAdjacency(remaining, edges, removedNode);

        Set<String> visited = new HashSet<>();
        List<Set<String>> components = new ArrayList<>();

        for (String start : remaining) {
            if (!visited.contains(start)) {
                Set<String> component = new HashSet<>();
                floodFill(start, adjacency, visited, component);
                components.add(component);
            }
        }

        if (components.size() <= 1) return List.of();

        int largestIdx = 0;
        int largestSize = 0;
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).size() > largestSize) {
                largestSize = components.get(i).size();
                largestIdx = i;
            }
        }

        List<NetworkSplitResult> results = new ArrayList<>();

        for (int i = 0; i < components.size(); i++) {
            Set<String> component = components.get(i);
            if (i == largestIdx) {
                nodesByNetwork.put(original, filterNodes(nodes, component));
                edgesByNetwork.put(original, filterEdges(edges, component, removedNode));
                for (String nodeId : component) {
                    nodeToNetwork.put(nodeId, original);
                }
            } else {
                NetworkId newId = NetworkId.create();
                nodesByNetwork.put(newId, filterNodes(nodes, component));
                edgesByNetwork.put(newId, filterEdges(edges, component, removedNode));
                for (String nodeId : component) {
                    nodeToNetwork.put(nodeId, newId);
                }
                results.add(new NetworkSplitResult(newId, component.stream()
                        .map(allNodes::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())));
            }
        }

        return results;
    }

    private Map<String, Set<String>> buildAdjacency(Set<String> nodeIds, Set<NetworkEdge> edges,
                                                      NetworkNode removedNode) {
        Map<String, Set<String>> adj = new HashMap<>();
        for (String id : nodeIds) {
            adj.put(id, new HashSet<>());
        }
        for (NetworkEdge edge : edges) {
            String src = edge.source().nodeId();
            String tgt = edge.target().nodeId();
            if (!src.equals(removedNode.nodeId()) && !tgt.equals(removedNode.nodeId())) {
                if (nodeIds.contains(src) && nodeIds.contains(tgt)) {
                    adj.get(src).add(tgt);
                    adj.get(tgt).add(src);
                }
            }
        }
        return adj;
    }

    private void floodFill(String node, Map<String, Set<String>> adj,
                           Set<String> visited, Set<String> component) {
        Deque<String> stack = new ArrayDeque<>();
        stack.push(node);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.add(current)) continue;
            component.add(current);
            for (String neighbor : adj.getOrDefault(current, Set.of())) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
    }

    private Map<String, NetworkNode> filterNodes(Map<String, NetworkNode> nodes, Set<String> keep) {
        Map<String, NetworkNode> result = new LinkedHashMap<>();
        for (String id : keep) {
            NetworkNode node = nodes.get(id);
            if (node != null) result.put(id, node);
        }
        return result;
    }

    private Set<NetworkEdge> filterEdges(Set<NetworkEdge> edges, Set<String> keep,
                                          NetworkNode removedNode) {
        return edges.stream()
                .filter(e -> keep.contains(e.source().nodeId())
                        && keep.contains(e.target().nodeId())
                        && !e.source().equals(removedNode)
                        && !e.target().equals(removedNode))
                .collect(Collectors.toSet());
    }

    public Optional<NetworkId> getNetworkFor(NetworkNode node) {
        return Optional.ofNullable(nodeToNetwork.get(node.nodeId()));
    }

    public Optional<NetworkNode> getNode(String nodeId) {
        return Optional.ofNullable(allNodes.get(nodeId));
    }

    public Set<NetworkNode> getNodes(NetworkId networkId) {
        Map<String, NetworkNode> nodes = nodesByNetwork.get(networkId);
        if (nodes == null) return Set.of();
        return new HashSet<>(nodes.values());
    }

    public Set<NetworkEdge> getEdges(NetworkId networkId) {
        Set<NetworkEdge> edges = edgesByNetwork.get(networkId);
        if (edges == null) return Set.of();
        return new HashSet<>(edges);
    }

    public int nodeCount(NetworkId networkId) {
        Map<String, NetworkNode> nodes = nodesByNetwork.get(networkId);
        return nodes != null ? nodes.size() : 0;
    }

    public int edgeCount(NetworkId networkId) {
        Set<NetworkEdge> edges = edgesByNetwork.get(networkId);
        return edges != null ? edges.size() : 0;
    }

    public Set<NetworkId> allNetworkIds() {
        return new HashSet<>(nodesByNetwork.keySet());
    }

    public void clear() {
        nodesByNetwork.clear();
        edgesByNetwork.clear();
        allNodes.clear();
        nodeToNetwork.clear();
    }

    public record NetworkSplitResult(NetworkId newNetworkId, Set<NetworkNode> detachedNodes) {}
}
