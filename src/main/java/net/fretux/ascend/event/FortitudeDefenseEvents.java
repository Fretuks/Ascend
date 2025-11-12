package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AscendMod.MODID)
public class FortitudeDefenseEvents {
    @SubscribeEvent
    public static void onHarmfulEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getEffectInstance() == null) return;
        if (event.getEffectInstance().getEffect().value().getCategory() != MobEffectCategory.HARMFUL) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        int fortitude = stats.getAttributeLevel("fortitude");
        if (fortitude <= 0) return;
        double perPoint = 0.004d;
        double max = 0.5d;
        double chance = Math.min(fortitude * perPoint, max);
        if (chance > 0 && player.getRandom().nextDouble() < chance) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickFortitudeCleanse(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        int fortitude = stats.getAttributeLevel("fortitude");
        if (fortitude <= 0) return;
        if (player.tickCount % 40 != 0) return;
        double perPoint = 0.003d;
        double max = 0.4d;
        double chance = Math.min(fortitude * perPoint, max);
        if (chance <= 0) return;
        if (player.getRandom().nextDouble() >= chance) return;
        player.getActiveEffects().stream()
                .filter(inst -> inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .findAny()
                .ifPresent(inst -> player.removeEffect(inst.getEffect()));
    }
}