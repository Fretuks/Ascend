package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class IntelligencePotionEvents {

    @SubscribeEvent
    public static void onBeneficialEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null) return;
        if (inst.getEffect().getCategory() != MobEffectCategory.BENEFICIAL) return;
        if (inst.getDuration() > 12000) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int intel = stats.getAttributeLevel("intelligence");
            if (intel <= 0) return;
            int baseDuration = inst.getDuration();
            double durPerPoint = 0.005d;
            double durMax = 0.5d;
            double durBonus = Math.min(intel * durPerPoint, durMax);
            int newDuration = (int) (baseDuration * (1.0d + durBonus));
            if (newDuration <= baseDuration) return;
            player.removeEffect(inst.getEffect());
            player.addEffect(new MobEffectInstance(
                    inst.getEffect(),
                    newDuration,
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            ));
        });
    }
}