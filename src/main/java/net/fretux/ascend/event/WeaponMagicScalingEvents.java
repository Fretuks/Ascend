package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class WeaponMagicScalingEvents {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (event.getEntity() == player) return;
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            float base = event.getAmount();
            float modified = base;
            if (isMagicDamage(event.getSource())) {
                int magicScaling = stats.getAttributeLevel("magic_scaling");
                if (magicScaling > 0) {
                    double bonusMult = StatEffects.getMagicScalingMultiplier(magicScaling);
                    modified = (float) (modified * bonusMult);
                }
            } else if (isRangedDamage(event.getSource())) {
                double rangedMultiplier = getRangedScalingMultiplier(stats, event.getSource());
                modified = (float) (modified * rangedMultiplier);
            }

            if (modified != base) {
                event.setAmount(modified);
            }
        });
    }

    private static boolean isMagicDamage(DamageSource source) {
        String id = source.getMsgId();
        if (id == null) return false;
        return id.equals("magic")
                || id.equals("indirectMagic")
                || id.equals("wither")
                || id.equals("dragonBreath")
                || id.equals("thrown")
                || id.equals("potion");
    }

    private static boolean isRangedDamage(DamageSource source) {
        return source.getDirectEntity() instanceof Projectile
                || source.getDirectEntity() instanceof AbstractArrow
                || source.getDirectEntity() instanceof ThrownTrident
                || source.getDirectEntity() instanceof FireworkRocketEntity;
    }

    private static double getRangedScalingMultiplier(
            net.fretux.ascend.player.PlayerStats stats,
            DamageSource source
    ) {
        double bonus = 1.0d;
        int agility = stats.getAttributeLevel("agility");
        if (agility > 0) {
            bonus *= StatEffects.getAgilityRangedDamageMultiplier(agility);
        }

        if (source.getDirectEntity() instanceof ThrownTrident) {
            int strength = stats.getAttributeLevel("strength");
            if (strength > 0) {
                bonus *= StatEffects.getStrengthTridentDamageMultiplier(strength);
            }
        }

        return bonus;
    }
}
