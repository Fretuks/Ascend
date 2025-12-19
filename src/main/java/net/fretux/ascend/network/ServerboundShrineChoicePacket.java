package net.fretux.ascend.network;

import net.fretux.ascend.compat.AscendMMCompat;
import net.fretux.ascend.item.RemembranceEssenceItem;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.mindmotion.player.PlayerCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

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
                switch (choice) {
                    case "forget" -> {
                        if (stats.getKnowledge() >= 15) {
                            stats.addKnowledge(-15);
                            stats.refundAllPoints();
                            net.fretux.ascend.player.StatEffects.applyAll(player);
                            player.sendSystemMessage(Component.literal("YOU ARE A BLANK SLATE. EMPTY. FORGOTTEN."));
                            RemembranceEssenceItem.consume(player);
                        } else {
                            player.sendSystemMessage(Component.literal("YOUR MIND IS TOO EMPTY TO BE WORTH MY TOUCH."));
                            player.hurt(player.level().damageSources().magic(), 4.0F);
                            Vec3 look = player.getLookAngle().normalize().scale(-2.0D);
                            player.setDeltaMovement(look.x * 5, 0.8, look.z * 5);
                            player.hurtMarked = true;
                        }
                        PlayerStatsProvider.sync(player);
                    }
                    case "understand" -> {
                        if (!AscendMMCompat.isMindMotionPresent()) return;
                        player.getCapability(PlayerCapabilityProvider.SANITY).ifPresent(sanity -> {
                            float sanityPercent = sanity.getSanity() / sanity.getMaxSanity();
                            if (sanityPercent >= 0.90f) {
                                stats.addKnowledge(5);
                                sanity.setSanity(0);
                                sanity.setInsanity(0);
                                player.sendSystemMessage(Component.literal("YOUR MIND EXPANDS AS IT BREAKS."));
                                RemembranceEssenceItem.consume(player);
                            } else {
                                player.sendSystemMessage(Component.literal("YOUR MIND IS NOT YET CLEAR ENOUGH."));
                            }
                        });
                        PlayerStatsProvider.sync(player);
                    }
                    case "rest" -> {
                        if (!AscendMMCompat.isMindMotionPresent()) return;
                        if (stats.getKnowledge() >= 10) {
                            stats.addKnowledge(-10);
                            player.getCapability(PlayerCapabilityProvider.SANITY).ifPresent(sanity -> {
                                sanity.setSanity(sanity.getMaxSanity());
                                sanity.setInsanity(0);
                            });
                            player.sendSystemMessage(Component.literal("THE SHRINE RESTORES YOUR FRACTURED MIND."));
                            RemembranceEssenceItem.consume(player);
                        } else {
                            player.sendSystemMessage(Component.literal("YOU LACK THE KNOWLEDGE TO REST."));
                        }
                        PlayerStatsProvider.sync(player);
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}