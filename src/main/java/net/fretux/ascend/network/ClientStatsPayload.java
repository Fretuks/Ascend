package net.fretux.ascend.network;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientStatsPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<ClientStatsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AscendMod.MODID, "client_stats_payload")
    );

    public static final StreamCodec<FriendlyByteBuf, ClientStatsPayload> CODEC = StreamCodec.of(
            ClientStatsPayload::write,
            ClientStatsPayload::read
    );

    public static void write(FriendlyByteBuf buffer, ClientStatsPayload payload) {
        buffer.writeNbt(payload.data);
    }

    public static ClientStatsPayload read(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new ClientStatsPayload(tag == null ? new CompoundTag() : tag);
    }

    public static void handleClient(ClientStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            var player = mc.player;
            if (player != null) {
                var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                    stats.deserializeNBT(payload.data);
                    StatEffects.applyAll(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
