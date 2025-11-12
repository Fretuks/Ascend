package net.fretux.ascend.network;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerboundShrineChoicePacket {
    private final String choice;

    public ServerboundShrineChoicePacket(String choice) {
        this.choice = choice;
    }

    public ServerboundShrineChoicePacket(FriendlyByteBuf buf) {
        this.choice = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(choice);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                if ("forget".equals(choice)) {
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
        });
        ctx.get().setPacketHandled(true);
    }
}