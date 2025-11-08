package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class IntelligenceEnchantmentEvents {

    @SubscribeEvent
    public static void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player player = serverLevel.getNearestPlayer(
                event.getPos().getX() + 0.5,
                event.getPos().getY() + 0.5,
                event.getPos().getZ() + 0.5,
                5.0,
                false
        );
        if (player == null) {
            return;
        }

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int intelligence = stats.getAttributeLevel("intelligence");
            if (intelligence <= 0) return;
            int original = event.getOriginalLevel();
            if (original <= 0) return;
            double perPoint = 0.005d;
            double max = 0.40d;
            double reduction = Math.min(intelligence * perPoint, max);
            int newLevel = (int) Math.max(1, Math.floor(original * (1.0d - reduction)));
            event.setEnchantLevel(newLevel);
        });
    }
}