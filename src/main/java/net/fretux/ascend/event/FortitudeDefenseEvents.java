package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class FortitudeDefenseEvents {
    @SubscribeEvent
    public static void onHarmfulEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getEffectInstance() == null) return;
        if (event.getEffectInstance().getEffect().getCategory() != MobEffectCategory.HARMFUL) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int fortitude = stats.getAttributeLevel("fortitude");
            if (fortitude <= 0) return;
            double perPoint = 0.004d;
            double max = 0.5d;
            double chance = Math.min(fortitude * perPoint, max);
            if (chance > 0 && player.getRandom().nextDouble() < chance) {
                event.setResult(Event.Result.DENY);
            }
        });
    }
    
    @SubscribeEvent
    public static void onPlayerTickFortitudeCleanse(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide || event.phase != TickEvent.Phase.END) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int fortitude = stats.getAttributeLevel("fortitude");
            if (fortitude <= 0) return;
            if (player.tickCount % 40 != 0) return;
            double perPoint = 0.003d;
            double max = 0.4d;
            double chance = Math.min(fortitude * perPoint, max);
            if (chance <= 0) return;
            if (player.getRandom().nextDouble() >= chance) return;
            player.getActiveEffects().stream()
                    .filter(inst -> inst.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                    .findAny()
                    .ifPresent(inst -> player.removeEffect(inst.getEffect()));
        });
    }
}