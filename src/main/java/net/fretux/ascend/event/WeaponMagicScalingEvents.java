package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.config.AscendConfig;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class WeaponMagicScalingEvents {
    private static double getScaling() {
        return AscendConfig.COMMON.attributeScalingMultiplier.get();
    }

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
                    double perPoint = 0.005d;
                    double maxBonus = 0.40d;
                    double bonusMult = 1.0d + Math.min(magicScaling * perPoint * getScaling(), maxBonus);
                    modified = (float) (modified * bonusMult);
                }
            } else if (isRangedDamage(event.getSource())) {
                double rangedMultiplier = getRangedScalingMultiplier(stats, event.getSource());
                modified = (float) (modified * rangedMultiplier);
            } else {
                double attackSpeed = 4.0d;
                if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
                    attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED).getValue();
                }
                String key =
                        (attackSpeed >= 1.8d) ? "light_scaling" :
                                (attackSpeed >= 1.2d) ? "medium_scaling" :
                                        "heavy_scaling";
                int level = stats.getAttributeLevel(key);
                if (level > 0) {
                    double perPoint =
                            key.equals("light_scaling") ? 0.003d :
                                    key.equals("medium_scaling") ? 0.004d :
                                            0.005d;
                    double maxBonus = 0.40d;
                    double bonusMult = 1.0d + Math.min(level * perPoint * getScaling(), maxBonus);
                    modified = (float) (modified * bonusMult);
                }
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
            double perPoint = 0.004d;
            double maxBonus = 0.35d;
            bonus *= 1.0d + Math.min(agility * perPoint * getScaling(), maxBonus);
        }

        if (source.getDirectEntity() instanceof ThrownTrident) {
            int strength = stats.getAttributeLevel("strength");
            if (strength > 0) {
                double perPoint = 0.003d;
                double maxBonus = 0.25d;
                bonus *= 1.0d + Math.min(strength * perPoint * getScaling(), maxBonus);
            }
        }

        return bonus;
    }
}