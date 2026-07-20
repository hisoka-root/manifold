package lol.hisoka.manifold;

import lol.hisoka.manifold.block.ManifoldRegistries;
import lol.hisoka.manifold.config.ManifoldConfig;
import lol.hisoka.manifold.energy.EnergyBudget;
import lol.hisoka.manifold.graph.NetworkGraph;
import lol.hisoka.manifold.graph.CraftingTable;
import lol.hisoka.manifold.graph.NetworkManager;
import lol.hisoka.manifold.network.ManifoldNetwork;
import lol.hisoka.manifold.routing.RequestQueue;
import lol.hisoka.manifold.routing.RoutingTable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Manifold.MOD_ID)
public final class Manifold {

    public static final String MOD_ID = "manifold";
    public static final Logger LOGGER = LoggerFactory.getLogger("Manifold");

    public Manifold(IEventBus modBus, ModContainer container) {
        NetworkGraph networkGraph = new NetworkGraph();
        RoutingTable routingTable = new RoutingTable(networkGraph);
        RequestQueue requestQueue = new RequestQueue();
        CraftingTable craftingTable = new CraftingTable();
        EnergyBudget energyBudget = new EnergyBudget();

        NetworkManager.init(networkGraph, routingTable, requestQueue, craftingTable, energyBudget);

        ManifoldRegistries.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, ManifoldConfig.SPEC);
        modBus.register(ManifoldNetwork.class);
        NeoForge.EVENT_BUS.register(ManifoldEvents.class);

        LOGGER.info("Manifold {} initialized", container.getModInfo().getVersion());
    }
}
