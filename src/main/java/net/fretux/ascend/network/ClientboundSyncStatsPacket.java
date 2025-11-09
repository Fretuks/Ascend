package net.fretux.ascend.network;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncStatsPacket {
    private final CompoundTag data;

    public ClientboundSyncStatsPacket(CompoundTag data) {
        this.data = data;
    }

    public ClientboundSyncStatsPacket(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        System.out.println("[SERVER] Spend packet handle() called");
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            var player = mc.player;
            if (player != null) {
                mc.player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                    stats.deserializeNBT(data);
                    StatEffects.applyAll(mc.player);
                    stats.deserializeNBT(data);
                });
            }
            ctx.get().setPacketHandled(true);
        });
    }
}
