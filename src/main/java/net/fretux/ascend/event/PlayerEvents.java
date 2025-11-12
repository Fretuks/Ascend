package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


@EventBusSubscriber(modid = AscendMod.MODID)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldStats = event.getOriginal().getData(PlayerStatsProvider.PLAYER_STATS);
                var newStats = event.getEntity().getData(PlayerStatsProvider.PLAYER_STATS);
                    newStats.deserializeNBT(oldStats.serializeNBT());

        if (!event.getEntity().level().isClientSide) {
            StatEffects.applyAll(event.getEntity());
            PlayerStatsProvider.sync(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            PlayerStatsProvider.sync(event.getEntity());
            StatEffects.applyAll(event.getEntity());
        }
    }
}