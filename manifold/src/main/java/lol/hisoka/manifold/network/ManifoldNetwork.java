package lol.hisoka.manifold.network;

import lol.hisoka.manifold.Manifold;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ManifoldNetwork {

    private ManifoldNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Manifold.MOD_ID).versioned("1");

        registrar.playToClient(
                ItemPositionPayload.TYPE,
                ItemPositionPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ItemPositionHandler::handleClient,
                        (payload, context) -> {}
                )
        );
    }
}
