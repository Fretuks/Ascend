package net.fretux.ascend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Supplier;

public class ServerboundSpendPointPacket {
    private final String attribute;

    public ServerboundSpendPointPacket(String attribute) {
        this.attribute = attribute;
    }

    public ServerboundSpendPointPacket(FriendlyByteBuf buf) {
        this.attribute = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(attribute);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            System.out.println("[Ascend] Received spend packet '" + attribute + "' from " + player.getGameProfile().getName());

            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                boolean success = stats.spendPoints(attribute);
                if (success) {
                    player.sendSystemMessage(Component.literal("You increased " + attribute + "!"));
                    CompoundTag data = stats.serializeNBT();
                    PacketHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new ClientboundSyncStatsPacket(data)
                    );
                } else {
                    int level = stats.getAttributeLevel(attribute);
                    int cost = stats.getCostToUpgrade(attribute);
                    int have = stats.getUnspentPoints();
                    player.sendSystemMessage(Component.literal(
                            "Not enough points (" + have + "/" + cost + ") to increase " + attribute + " (lvl " + level + ")."
                    ));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

}