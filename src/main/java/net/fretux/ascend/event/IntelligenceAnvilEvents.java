package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class IntelligenceAnvilEvents {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;
        int baseCost = event.getCost();
        if (baseCost <= 0) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int intelligence = stats.getAttributeLevel("intelligence");
            if (intelligence <= 0) return;
            double reduction = StatEffects.getIntelligenceAnvilCostReduction(intelligence);
            if (reduction <= 0.0d) return;
            int newCost = (int) Math.max(1, Math.floor(baseCost * (1.0d - reduction)));
            if (newCost < baseCost) {
                event.setCost(newCost);
            }
        });
    }
}
