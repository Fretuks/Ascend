package net.fretux.ascend.network;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record ServerStatsPayload(String attribute) implements CustomPacketPayload {
    public static final Type<ServerStatsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AscendMod.MODID, "server_stats_payload")
    );

    public static final StreamCodec<FriendlyByteBuf, ServerStatsPayload> CODEC = StreamCodec.of(
            ServerStatsPayload::write,
            ServerStatsPayload::read
    );

    public static void write(FriendlyByteBuf buffer, ServerStatsPayload payload) {
        buffer.writeUtf(payload.attribute);
    }

    public static ServerStatsPayload read(FriendlyByteBuf buffer) {
        return new ServerStatsPayload(buffer.readUtf());
    }

    public static void handleServer(ServerStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                boolean success = stats.spendPoints(payload.attribute);
                if (success) {
                    StatEffects.applyAll(player);
                    CompoundTag data = stats.serializeNBT();
                    PacketDistributor.sendToPlayer(player, new ClientStatsPayload(data));
                }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
