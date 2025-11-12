package net.fretux.ascend.player;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.network.ClientStatsPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PlayerStatsProvider {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AscendMod.MODID);

    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS = ATTACHMENT_TYPES.register(
            "player_stats",
            () -> AttachmentType.<PlayerStats>builder(PlayerStats::new)
                    .serialize(new PlayerStats.Serializer())
                    .build()
    );

    public static void sync(Player player) {
        PlayerStats stats = player.getData(PLAYER_STATS);
        CompoundTag data = stats.serializeNBT();
        PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new ClientStatsPayload(data));
    }
}