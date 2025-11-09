package net.fretux.ascend.network;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
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
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                boolean success = stats.spendPoints(attribute);
                if (success) {
                    StatEffects.applyAll(player);
                    CompoundTag data = stats.serializeNBT();
                    PacketHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new ClientboundSyncStatsPacket(data)
                    );
                }
            });
        });
        context.setPacketHandled(true);
    }
}