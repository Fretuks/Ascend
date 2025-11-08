package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class PlayerEvents {

    private static final ResourceLocation PLAYER_STATS_ID =
            new ResourceLocation(AscendMod.MODID, "player_stats");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(PLAYER_STATS_ID, new PlayerStatsProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(oldStats ->
                event.getEntity().getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(newStats ->
                        newStats.deserializeNBT(oldStats.serializeNBT())));
    }

    // Optional but very helpful: sync on login so client sees initial 15 points
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            PlayerStatsProvider.sync(event.getEntity());
        }
    }
}
