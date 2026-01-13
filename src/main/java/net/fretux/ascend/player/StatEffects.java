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
    
    private static double mediumScalingPercent(int x) {
        if (x <= 0) return 0.0d;
        return Math.pow(3.5d * x, 0.61d);
    }

    private static double lightScalingPercent(int x) {
        if (x <= 0) return 0.0d;
        return Math.pow(4.0d * x, 0.55d);
    }

    private static double heavyScalingPercent(int x) {
        if (x <= 0) return 0.0d;
        return (0.4d * x) + (5.0d * Math.sin(0.065d * x));
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
                double dmgBonus = (mediumScalingPercent(strengthLevel) * 0.1d) * getScaling();
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
                double kbBonus = (mediumScalingPercent(strengthLevel) * 0.015d) * getScaling();
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
                double bonus = agilityLevel * 0.0005d * getScaling();
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
                double healthBonus = fortitudeLevel * 0.25d * getScaling();
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
                double res = Math.min(0.006d * fortitudeLevel * getScaling(), 0.60d);
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
        double reduction = Math.min(willpowerLevel * 0.005d * getScaling(), 0.5d);
        return (float) (1.0d - reduction);
    }

    public static float getWillpowerTempoMultiplier(int willpowerLevel) {
        if (willpowerLevel <= 0) return 1.0f;
        double bonus = Math.min(willpowerLevel * 0.004d * getScaling(), 0.4d);
        return (float) (1.0d + bonus);
    }

    public static float getWillpowerHealthRegen(int willpowerLevel) {
        if (willpowerLevel <= 0) return 0.0f;
        double bonus = Math.min(willpowerLevel * 0.01d * getScaling(), 2.0d);
        return (float) bonus;
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
        return Math.max(0, intelligenceLevel);
    }

    public static double getIntelligenceAnvilCostReduction(int intelligenceLevel) {
        if (intelligenceLevel <= 0) return 0.0d;
        double perPoint = 0.005d;
        double max = 0.40d;
        return Math.min(intelligenceLevel * perPoint * getScaling(), max);
    }

    public static int getIntelligenceKnowledgeGain(int intelligenceLevel, int baseGain) {
        if (baseGain <= 0) return baseGain;
        if (intelligenceLevel <= 0) return baseGain;
        double perPoint = 0.005d;
        double max = 0.50d;
        double bonus = Math.min(intelligenceLevel * perPoint * getScaling(), max);
        int extra = (int) Math.floor(baseGain * bonus);
        return baseGain + Math.max(0, extra);
    }

    public static double getCharismaTradeDiscount(int charismaLevel) {
        if (charismaLevel <= 0) return 0.0d;
        double perPoint = 0.01d;
        double max = 0.30d;
        return Math.min(charismaLevel * perPoint * getScaling(), max);
    }

    public static int getCharismaVillagerReputationBonus(int charismaLevel) {
        if (charismaLevel <= 0) return 0;
        double perPoint = 0.1d;
        double max = 10.0d;
        return (int) Math.floor(Math.min(charismaLevel * perPoint * getScaling(), max));
    }

    public static double getCharismaAllyHealthBonus(int charismaLevel) {
        if (charismaLevel <= 0) return 0.0d;
        double perPoint = 0.0025d;
        double max = 0.25d;
        return Math.min(charismaLevel * perPoint * getScaling(), max);
    }

    public static double getCharismaAllyDamageBonus(int charismaLevel) {
        if (charismaLevel <= 0) return 0.0d;
        double perPoint = 0.0020d;
        double max = 0.20d;
        return Math.min(charismaLevel * perPoint * getScaling(), max);
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
        if (scalingLevel <= 0) return 1.0d;
        double percent = mediumScalingPercent(scalingLevel);
        return 1.0d + (percent / 100.0d);
    }
    
    public static double getLightWeaponScalingMultiplier(int scalingLevel) {
        if (scalingLevel <= 0) return 1.0d;
        double percent = lightScalingPercent(scalingLevel);
        return 1.0d + (percent / 100.0d);
    }
    
    public static double getHeavyWeaponScalingMultiplier(int scalingLevel) {
        if (scalingLevel <= 0) return 1.0d;
        double percent = heavyScalingPercent(scalingLevel);
        return 1.0d + (percent / 100.0d);
    }
    
    public static double getMagicScalingMultiplier(int scalingLevel) {
        if (scalingLevel <= 0) return 1.0d;
        double perPoint = 0.0075d;
        double maxBonus = 0.75d;
        return 1.0d + Math.min(scalingLevel * perPoint, maxBonus);
    }
}
