package net.fretux.ascend.network;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerShrinePayload(String choice) implements CustomPacketPayload {
    public static final Type<ServerShrinePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AscendMod.MODID, "server_shrine_payload")
    );

    public static final StreamCodec<FriendlyByteBuf, ServerShrinePayload> CODEC = StreamCodec.of(
            ServerShrinePayload::write,
            ServerShrinePayload::read
    );

    public static void write(FriendlyByteBuf buffer, ServerShrinePayload payload) {
        buffer.writeUtf(payload.choice);
    }

    public static ServerShrinePayload read(FriendlyByteBuf buffer) {
        String choice = buffer.readUtf();
        return new ServerShrinePayload(choice == null ? new String() : choice);
    }

    public static void handleServer(ServerShrinePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;

            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                if ("forget".equals(payload.choice)) {
                    if (stats.getKnowledge() >= 15) {
                        stats.addKnowledge(-15);
                        stats.refundAllPoints();
                        player.sendSystemMessage(Component.literal("FOOLISH... YET HONEST."));
                    } else {
                        player.sendSystemMessage(Component.literal("YOUR MIND IS TOO EMPTY TO BE WORTH MY TOUCH."));
                        player.hurt(player.level().damageSources().magic(), 4.0F);
                        Vec3 look = player.getLookAngle().normalize().scale(-2.0D);
                        player.setDeltaMovement(look.x * 5, 0.8, look.z * 5);
                        player.hurtMarked = true;
                    }
                    PlayerStatsProvider.sync(player);
                }
            });
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
