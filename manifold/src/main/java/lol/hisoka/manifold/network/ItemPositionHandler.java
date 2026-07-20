package lol.hisoka.manifold.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

final class ItemPositionHandler {

    static void handleClient(ItemPositionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            ClientItemTracker.get().updatePosition(
                    payload.transitId(),
                    payload.startPos(),
                    payload.endPos(),
                    payload.progress(),
                    payload.item()
            );
        });
    }
}
