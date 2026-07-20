package lol.hisoka.manifold.energy;

import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;
import lol.hisoka.manifold.capability.CapabilityEndpoint;
import lol.hisoka.manifold.config.ManifoldConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EnergyBudget {

    private static final int SCAN_INTERVAL_TICKS = 20;

    private final Map<NetworkId, NetworkEnergyState> budgets = new ConcurrentHashMap<>();
    private final Map<NetworkId, Integer> scanTimers = new ConcurrentHashMap<>();

    public void contribute(NetworkId networkId, long energyAmount) {
        budgets.computeIfAbsent(networkId, k -> new NetworkEnergyState())
                .addEnergy(energyAmount);
    }

    public void scanAndCollect(NetworkId networkId, Set<NetworkNode> nodes,
                                ServerLevel level, int tickCounter) {
        int lastScan = scanTimers.getOrDefault(networkId, 0);
        if (tickCounter - lastScan < SCAN_INTERVAL_TICKS) return;
        scanTimers.put(networkId, tickCounter);

        for (NetworkNode node : nodes) {
            if (!(node instanceof NetworkNode.EndpointWrapper wrapper)) continue;
            if (!(wrapper.endpoint() instanceof CapabilityEndpoint capEndpoint)) continue;

            IEnergyStorage energy = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    capEndpoint.id().pos(),
                    capEndpoint.facing()
            );
            if (energy != null && energy.canExtract() && energy.getEnergyStored() > 0) {
                int extracted = energy.extractEnergy(
                        Math.min(energy.getEnergyStored(), 1000), false
                );
                if (extracted > 0) {
                    contribute(networkId, extracted);
                }
            }
        }
    }

    public boolean consume(NetworkId networkId, int itemCount) {
        NetworkEnergyState state = budgets.computeIfAbsent(networkId, k -> new NetworkEnergyState());
        int costPerItem = ManifoldConfig.FE_COST_PER_ITEM.get();
        long totalCost = (long) itemCount * costPerItem;

        if (state.availableEnergy.get() >= totalCost) {
            state.availableEnergy.addAndGet(-totalCost);
            return true;
        }

        if (ManifoldConfig.GRACEFUL_DEGRADATION.get()) {
            state.degrade(itemCount);
            return false;
        }

        return false;
    }

    public long getAvailable(NetworkId networkId) {
        NetworkEnergyState state = budgets.get(networkId);
        return state != null ? state.availableEnergy.get() : 0L;
    }

    public int getDegradationMultiplier(NetworkId networkId) {
        NetworkEnergyState state = budgets.get(networkId);
        return state != null ? state.degradationMultiplier.get() : 1;
    }

    public void clear(NetworkId networkId) {
        budgets.remove(networkId);
        scanTimers.remove(networkId);
    }

    public void clearAll() {
        budgets.clear();
        scanTimers.clear();
    }

    private static final class NetworkEnergyState {
        final AtomicLong availableEnergy = new AtomicLong();
        final AtomicInteger degradationMultiplier = new AtomicInteger(1);

        void addEnergy(long amount) {
            availableEnergy.accumulateAndGet(amount, (a, b) -> Math.min(a + b, Long.MAX_VALUE));
            degradationMultiplier.set(1);
        }

        void degrade(int itemCount) {
            degradationMultiplier.accumulateAndGet(itemCount, Math::min);
        }
    }
}

