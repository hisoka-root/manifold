package lol.hisoka.manifold.network;

import lol.hisoka.manifold.routing.ItemTransitManager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NetworkSender {

    private NetworkSender() {}

    public static void broadcastItemPositions(ServerLevel level, ItemTransitManager transitManager) {
        for (ItemTransitManager.InTransitItem transitItem : transitManager.allItems()) {
            var edge = transitItem.currentEdge();
            if (edge == null) continue;

            BlockPos midPos = midPoint(edge.source().nodeId(), edge.target().nodeId());
            if (midPos == null) continue;

            LevelChunk chunk = level.getChunkSource().getChunkNow(
                    midPos.getX() >> 4, midPos.getZ() >> 4
            );
            if (chunk == null) continue;

            for (ServerPlayer player : level.players()) {
                if (!player.isSpectator() && chunkDistance(player, chunk) <= 4) {
                    ItemPositionPayload payload = new ItemPositionPayload(
                            transitItem.id(),
                            decodePos(edge.source().nodeId()),
                            decodePos(edge.target().nodeId()),
                            transitItem.progress(),
                            transitItem.stack()
                    );
                    PacketDistributor.sendToPlayer(player, payload);
                }
            }
        }
    }

    private static BlockPos midPoint(String nodeIdA, String nodeIdB) {
        BlockPos a = decodePos(nodeIdA);
        BlockPos b = decodePos(nodeIdB);
        if (a == null || b == null) return null;
        return new BlockPos(
                (a.getX() + b.getX()) / 2,
                (a.getY() + b.getY()) / 2,
                (a.getZ() + b.getZ()) / 2
        );
    }

    private static BlockPos decodePos(String nodeId) {
        try {
            String[] parts = nodeId.contains(":") ? nodeId.split(":")[1].split(",") : nodeId.split(",");
            if (parts.length >= 3) {
                return new BlockPos(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                );
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static double chunkDistance(ServerPlayer player, LevelChunk chunk) {
        int px = player.chunkPosition().x;
        int pz = player.chunkPosition().z;
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        return Math.sqrt((px - cx) * (px - cx) + (pz - cz) * (pz - cz));
    }
}
