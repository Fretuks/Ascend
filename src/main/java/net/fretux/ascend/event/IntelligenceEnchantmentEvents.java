package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;

@EventBusSubscriber(modid = AscendMod.MODID)
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

        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        int intelligence = stats.getAttributeLevel("intelligence");
        if (intelligence <= 0) return;
        int original = event.getOriginalLevel();
        if (original <= 0) return;
        double perPoint = 0.005d;
        double max = 0.40d;
        double reduction = Math.min(intelligence * perPoint, max);
        int newLevel = (int) Math.max(1, Math.floor(original * (1.0d - reduction)));
        event.setEnchantLevel(newLevel);
    }
}