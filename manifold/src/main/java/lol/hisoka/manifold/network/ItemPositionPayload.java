package lol.hisoka.manifold.network;

import lol.hisoka.manifold.Manifold;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ItemPositionPayload(
        UUID transitId,
        BlockPos startPos,
        BlockPos endPos,
        float progress,
        ItemStack item
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ItemPositionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Manifold.MOD_ID, "item_position"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPositionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, ItemPositionPayload::transitId,
                    BlockPos.STREAM_CODEC, ItemPositionPayload::startPos,
                    BlockPos.STREAM_CODEC, ItemPositionPayload::endPos,
                    ByteBufCodecs.FLOAT, ItemPositionPayload::progress,
                    ItemStack.STREAM_CODEC, ItemPositionPayload::item,
                    ItemPositionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
