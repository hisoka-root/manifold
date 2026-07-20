package lol.hisoka.manifold;

import lol.hisoka.manifold.graph.NetworkManager;
import lol.hisoka.manifold.network.NetworkSender;
import lol.hisoka.manifold.persistence.NetworkSavedData;
import lol.hisoka.manifold.routing.RoutingTable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class ManifoldEvents {

    private ManifoldEvents() {}

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        NetworkManager manager = NetworkManager.get();
        if (manager != null) {
            RoutingTable rt = manager.routingTable();
            if (rt != null) {
                rt.shutdown();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        NetworkManager manager = NetworkManager.get();
        if (manager == null) return;

        manager.serverTick(serverLevel);

        NetworkSender.broadcastItemPositions(serverLevel, manager.transitManager());
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof Level level)) return;

        NetworkManager manager = NetworkManager.get();
        if (manager == null) return;

        manager.onBlockPlaced(level, event.getPos());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof Level level)) return;

        NetworkManager manager = NetworkManager.get();
        if (manager == null) return;

        manager.onBlockRemoved(level, event.getPos());
    }
}
