package net.fretux.ascend.player;

import net.fretux.ascend.network.ClientboundSyncStatsPacket;
import net.fretux.ascend.network.PacketHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerStatsProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<PlayerStats> PLAYER_STATS = CapabilityManager.get(new CapabilityToken<>(){});
    private final PlayerStats stats = new PlayerStats();
    private final LazyOptional<PlayerStats> optional = LazyOptional.of(() -> stats);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_STATS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return stats.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        stats.deserializeNBT(nbt);
    }

    public static void sync(Player player) {
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            CompoundTag data = stats.serializeNBT();
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new ClientboundSyncStatsPacket(data));
        });
    }

}
