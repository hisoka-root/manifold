package lol.hisoka.manifold.routing;

import lol.hisoka.manifold.api.network.EndpointId;
import lol.hisoka.manifold.api.network.NetworkId;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RequestQueue {

    private final ConcurrentMap<NetworkId, Queue<PendingRequest>> queues = new ConcurrentHashMap<>();

    public void enqueue(NetworkId networkId, EndpointId requester, ItemStack requested, int count) {
        queues.computeIfAbsent(networkId, k -> new ArrayDeque<>())
                .add(new PendingRequest(requester, requested, count));
    }

    public Queue<PendingRequest> drainMailbox(NetworkId networkId) {
        Queue<PendingRequest> mailbox = queues.get(networkId);
        if (mailbox == null) return new ArrayDeque<>();
        Queue<PendingRequest> snapshot = new ArrayDeque<>(mailbox);
        mailbox.clear();
        return snapshot;
    }

    public int pendingCount(NetworkId networkId) {
        Queue<PendingRequest> q = queues.get(networkId);
        return q != null ? q.size() : 0;
    }

    public void clear(NetworkId networkId) {
        queues.remove(networkId);
    }

    public void clearAll() {
        queues.clear();
    }

    public record PendingRequest(EndpointId requester, ItemStack requested, int count) {}
}
