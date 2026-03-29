package net.fretux.ascend.player;

import net.fretux.ascend.compat.IronsSpellbooksCompat;
import net.fretux.ascend.config.AscendConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class StatEffects {

    private static final UUID STRENGTH_DAMAGE_UUID =
            UUID.fromString("a7b8b2c3-9b0d-4e1a-8ee3-21f6a7b2c3d4");
    private static final UUID STRENGTH_KNOCKBACK_UUID =
            UUID.fromString("b17f0d74-9cd9-4f90-9ec8-6a1046e2f912");

    private static final UUID AGILITY_SPEED_UUID =
            UUID.fromString("c2f4e9a1-3b8d-4f21-9d44-8176f8791a11");

    private static final UUID FORTITUDE_HEALTH_UUID =
            UUID.fromString("d3a4b5c6-7d8e-4f90-8a1b-223344556677");
    private static final UUID FORTITUDE_KB_RESIST_UUID =
            UUID.fromString("e4b5c6d7-8e9f-4012-9abc-334455667788");
    private static final UUID CHARISMA_ALLY_HEALTH_UUID =
            UUID.fromString("f5c6d7e8-9012-4bcd-9ef0-445566778899");
    private static final UUID CHARISMA_ALLY_DAMAGE_UUID =
            UUID.fromString("0a1b2c3d-4e5f-4012-8a9b-556677889900");

    private StatEffects() {
    }

    private static double getScaling() {
        return AscendConfig.COMMON.attributeScalingMultiplier.get();
    }

    private static double scaledRatio(int level, double perPoint, double max) {
        if (level <= 0) return 0.0d;
        return Math.min(level * perPoint * getScaling(), max);
    }

    private static double scalingMultiplier(int level, double perPoint, double maxBonus) {
        return 1.0d + scaledRatio(level, perPoint, maxBonus);
    }

    public static void applyAll(Player player) {
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            applyStrength(player, stats.getAttributeLevel("strength"));
            applyAgility(player, stats.getAttributeLevel("agility"));
            applyFortitude(player, stats.getAttributeLevel("fortitude"));
            if (IronsSpellbooksCompat.isLoaded()) {
                IronsSpellbooksCompat.applyAll(player);
            }
        });
    }

    public static void applyStrength(Player player, int strengthLevel) {
        // Always remove first so resets correctly.
        AttributeInstance atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(STRENGTH_DAMAGE_UUID);
            if (strengthLevel > 0) {
                double dmgBonus = Math.min(strengthLevel * 0.06d * getScaling(), 6.0d);
                atk.addTransientModifier(new AttributeModifier(
                        STRENGTH_DAMAGE_UUID,
                        "Ascend Strength damage bonus",
                        dmgBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        AttributeInstance kb = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (kb != null) {
            kb.removeModifier(STRENGTH_KNOCKBACK_UUID);
            if (strengthLevel > 0) {
                double kbBonus = scaledRatio(strengthLevel, 0.002d, 0.2d);
                kb.addTransientModifier(new AttributeModifier(
                        STRENGTH_KNOCKBACK_UUID,
                        "Ascend Strength knockback bonus",
                        kbBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    public static void applyAgility(Player player, int agilityLevel) {
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            move.removeModifier(AGILITY_SPEED_UUID);
            if (agilityLevel > 0) {
                double bonus = scaledRatio(agilityLevel, 0.0008d, 0.08d);
                move.addTransientModifier(new AttributeModifier(
                        AGILITY_SPEED_UUID,
                        "Ascend Agility speed bonus",
                        bonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    public static void applyFortitude(Player player, int fortitudeLevel) {
        AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(FORTITUDE_HEALTH_UUID);
            if (fortitudeLevel > 0) {
                double healthBonus = Math.min(fortitudeLevel * 0.18d * getScaling(), 18.0d);
                hp.addTransientModifier(new AttributeModifier(
                        FORTITUDE_HEALTH_UUID,
                        "Ascend Fortitude health bonus",
                        healthBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
        AttributeInstance kbRes = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null) {
            kbRes.removeModifier(FORTITUDE_KB_RESIST_UUID);
            if (fortitudeLevel > 0) {
                double res = scaledRatio(fortitudeLevel, 0.003d, 0.3d);
                kbRes.addTransientModifier(new AttributeModifier(
                        FORTITUDE_KB_RESIST_UUID,
                        "Ascend Fortitude knockback resist",
                        res,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    public static float getWillpowerSanityMultiplier(int willpowerLevel) {
        if (willpowerLevel <= 0) return 1.0f;
        double reduction = scaledRatio(willpowerLevel, 0.0035d, 0.35d);
        return (float) (1.0d - reduction);
    }

    public static float getWillpowerTempoMultiplier(int willpowerLevel) {
        if (willpowerLevel <= 0) return 1.0f;
        double bonus = scaledRatio(willpowerLevel, 0.003d, 0.3d);
        return (float) (1.0d + bonus);
    }

    public static float getWillpowerHealthRegen(int willpowerLevel) {
        if (willpowerLevel <= 0) return 0.0f;
        double bonus = scaledRatio(willpowerLevel, 0.0125d, 1.25d);
        return (float) bonus;
    }

    public static double getWillpowerCastTimeReduction(int willpowerLevel) {
        return scaledRatio(willpowerLevel, 0.002d, 0.2d);
    }

    public static float getPlayerSanityMultiplier(Player player) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                .map(stats -> getWillpowerSanityMultiplier(stats.getAttributeLevel("willpower")))
                .orElse(1.0f);
    }

    public static float getPlayerTempoMultiplier(Player player) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                .map(stats -> getWillpowerTempoMultiplier(stats.getAttributeLevel("willpower")))
                .orElse(1.0f);
    }

    public static int getIntelligenceManaBonus(int intelligenceLevel) {
        return (int) Math.floor(Math.min(intelligenceLevel * 2.0d * getScaling(), 200.0d));
    }

    public static double getIntelligenceAnvilCostReduction(int intelligenceLevel) {
        return scaledRatio(intelligenceLevel, 0.003d, 0.3d);
    }

    public static int getIntelligenceKnowledgeGain(int intelligenceLevel, int baseGain) {
        if (baseGain <= 0) return baseGain;
        if (intelligenceLevel <= 0) return baseGain;
        double bonus = scaledRatio(intelligenceLevel, 0.003d, 0.3d);
        int extra = (int) Math.floor(baseGain * bonus);
        return baseGain + Math.max(0, extra);
    }

    public static double getCharismaTradeDiscount(int charismaLevel) {
        return scaledRatio(charismaLevel, 0.003d, 0.3d);
    }

    public static int getCharismaVillagerReputationBonus(int charismaLevel) {
        if (charismaLevel <= 0) return 0;
        return (int) Math.floor(Math.min(charismaLevel * 0.08d * getScaling(), 8.0d));
    }

    public static double getCharismaAllyHealthBonus(int charismaLevel) {
        return scaledRatio(charismaLevel, 0.002d, 0.2d);
    }

    public static double getCharismaAllyDamageBonus(int charismaLevel) {
        return scaledRatio(charismaLevel, 0.002d, 0.2d);
    }

    public static double getFortitudeEffectGuardChance(int fortitudeLevel) {
        return scaledRatio(fortitudeLevel, 0.0025d, 0.25d);
    }

    public static double getFortitudeCleanseChance(int fortitudeLevel) {
        return scaledRatio(fortitudeLevel, 0.002d, 0.2d);
    }

    public static double getAgilityEvasionChance(int agilityLevel) {
        return scaledRatio(agilityLevel, 0.0015d, 0.15d);
    }

    public static double getAgilityRangedDamageMultiplier(int agilityLevel) {
        return 1.0d + Math.min((getLightWeaponScalingMultiplier(agilityLevel) - 1.0d) * 0.7d, 0.25d);
    }

    public static double getStrengthTridentDamageMultiplier(int strengthLevel) {
        return 1.0d + Math.min((getWeaponScalingMultiplier(strengthLevel) - 1.0d) * 0.6d, 0.2d);
    }

    public static void applyCharismaAllyBuff(LivingEntity entity, int charismaLevel) {
        if (charismaLevel <= 0) {
            clearCharismaAllyBuff(entity);
            return;
        }
        double healthBonus = getCharismaAllyHealthBonus(charismaLevel);
        AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(CHARISMA_ALLY_HEALTH_UUID);
            if (healthBonus > 0.0d) {
                health.addTransientModifier(new AttributeModifier(
                        CHARISMA_ALLY_HEALTH_UUID,
                        "Ascend Charisma ally max health",
                        healthBonus,
                        AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }
        double damageBonus = getCharismaAllyDamageBonus(charismaLevel);
        AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(CHARISMA_ALLY_DAMAGE_UUID);
            if (damageBonus > 0.0d) {
                damage.addTransientModifier(new AttributeModifier(
                        CHARISMA_ALLY_DAMAGE_UUID,
                        "Ascend Charisma ally damage",
                        damageBonus,
                        AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }
    }

    public static void clearCharismaAllyBuff(LivingEntity entity) {
        AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(CHARISMA_ALLY_HEALTH_UUID);
        }
        AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(CHARISMA_ALLY_DAMAGE_UUID);
        }
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
    
    public static double getWeaponScalingMultiplier(int scalingLevel) {
        return scalingMultiplier(scalingLevel, 0.0035d, 0.35d);
    }

    public static double getLightWeaponScalingMultiplier(int scalingLevel) {
        return scalingMultiplier(scalingLevel, 0.0035d, 0.35d);
    }

    public static double getHeavyWeaponScalingMultiplier(int scalingLevel) {
        return scalingMultiplier(scalingLevel, 0.0035d, 0.35d);
    }

    public static double getMagicScalingMultiplier(int scalingLevel) {
        return scalingMultiplier(scalingLevel, 0.0035d, 0.35d);
    }
}
