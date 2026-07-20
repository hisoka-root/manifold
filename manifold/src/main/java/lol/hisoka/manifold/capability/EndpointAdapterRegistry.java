package lol.hisoka.manifold.capability;

import lol.hisoka.manifold.api.endpoint.Endpoint;
import lol.hisoka.manifold.api.endpoint.EndpointAdapter;
import lol.hisoka.manifold.api.endpoint.EndpointType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EndpointAdapterRegistry {

    private static final EndpointAdapterRegistry INSTANCE = new EndpointAdapterRegistry();

    private final List<EndpointAdapter> adapters = new CopyOnWriteArrayList<>();

    private EndpointAdapterRegistry() {
        register(new CapabilityEndpointAdapter());
    }

    public static EndpointAdapterRegistry get() {
        return INSTANCE;
    }

    public void register(EndpointAdapter adapter) {
        int index = 0;
        for (int i = 0; i < adapters.size(); i++) {
            if (adapters.get(i).priority() > adapter.priority()) {
                break;
            }
            index = i + 1;
        }
        adapters.add(index, adapter);
    }

    public boolean unregister(EndpointAdapter adapter) {
        return adapters.remove(adapter);
    }

    public Optional<Endpoint> tryCreate(Level level, BlockPos pos, Direction side) {
        return tryCreate(level, pos, side, EndpointType.PROVIDER);
    }

    public Optional<Endpoint> tryCreate(Level level, BlockPos pos, Direction side, EndpointType hintType) {
        for (EndpointAdapter adapter : adapters) {
            if (adapter instanceof CapabilityEndpointAdapter capAdapter) {
                Optional<Endpoint> endpoint = capAdapter.tryCreate(level, pos, side, hintType);
                if (endpoint.isPresent()) return endpoint;
            } else {
                Optional<Endpoint> endpoint = adapter.tryCreate(level, pos, side);
                if (endpoint.isPresent()) return endpoint;
            }
        }
        return Optional.empty();
    }

    public List<EndpointAdapter> all() {
        return List.copyOf(adapters);
    }
}
