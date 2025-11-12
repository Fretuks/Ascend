package net.fretux.ascend.player;

import net.fretux.ascend.compat.IronsSpellbooksCompat;
import net.fretux.ascend.config.AscendConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class StatEffects {

    private static final ResourceLocation STRENGTH_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "strength_damage");
    private static final ResourceLocation STRENGTH_KNOCKBACK_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "strength_knockback");

    private static final ResourceLocation AGILITY_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "agility_speed");

    private static final ResourceLocation FORTITUDE_HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "fortitude_health");
    private static final ResourceLocation FORTITUDE_KB_RESIST_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "fortitude_kb_resist");

    private StatEffects() {
    }

    private static double getScaling() {
        return AscendConfig.COMMON.attributeScalingMultiplier.get();
    }

    public static void applyAll(Player player) {
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        applyStrength(player, stats.getAttributeLevel("strength"));
        applyAgility(player, stats.getAttributeLevel("agility"));
        applyFortitude(player, stats.getAttributeLevel("fortitude"));
        // - Willpower: see getWillpowerSanityMultiplier / getWillpowerTempoMultiplier.
        if (IronsSpellbooksCompat.isLoaded()) {
            IronsSpellbooksCompat.applyAll(player);
        }
    }

    public static void applyStrength(Player player, int strengthLevel) {
        AttributeInstance atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(STRENGTH_DAMAGE_ID);
            if (strengthLevel > 0) {
                double dmgBonus = strengthLevel * 0.1d * getScaling();
                atk.addTransientModifier(new AttributeModifier(
                        STRENGTH_DAMAGE_ID,
                        dmgBonus,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        // Knockback
        AttributeInstance kb = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (kb != null) {
            kb.removeModifier(STRENGTH_KNOCKBACK_ID);
            if (strengthLevel > 0) {
                double kbBonus = strengthLevel * 0.015d * getScaling();
                kb.addTransientModifier(new AttributeModifier(
                        STRENGTH_KNOCKBACK_ID,
                        kbBonus,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    public static void applyAgility(Player player, int agilityLevel) {
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            move.removeModifier(AGILITY_SPEED_ID);
            if (agilityLevel > 0) {
                double bonus = agilityLevel * 0.0005d * getScaling();
                move.addTransientModifier(new AttributeModifier(
                        AGILITY_SPEED_ID,
                        bonus,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    public static void applyFortitude(Player player, int fortitudeLevel) {
        AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(FORTITUDE_HEALTH_ID);
            if (fortitudeLevel > 0) {
                double healthBonus = fortitudeLevel * 0.25d * getScaling();
                hp.addTransientModifier(new AttributeModifier(
                        FORTITUDE_HEALTH_ID,
                        healthBonus,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
        AttributeInstance kbRes = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null) {
            kbRes.removeModifier(FORTITUDE_KB_RESIST_ID);
            if (fortitudeLevel > 0) {
                double res = Math.min(0.006d * fortitudeLevel * getScaling(), 0.60d);
                kbRes.addTransientModifier(new AttributeModifier(
                        FORTITUDE_KB_RESIST_ID,
                        res,
                        AttributeModifier.Operation.ADD_VALUE
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

    public static float getPlayerSanityMultiplier(Player player) {
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        return getWillpowerSanityMultiplier(stats.getAttributeLevel("willpower"));
    }

    public static float getPlayerTempoMultiplier(Player player) {
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        return getWillpowerTempoMultiplier(stats.getAttributeLevel("willpower"));
    }

    public static int getIntelligenceManaBonus(int intelligenceLevel) {
        return Math.max(0, intelligenceLevel);
    }

    public static double getCharismaTradeDiscount(int charismaLevel) {
        if (charismaLevel <= 0) return 0.0d;
        double perPoint = 0.01d;
        double max = 0.30d;
        return Math.min(charismaLevel * perPoint * getScaling(), max);
    }

    public static double getWeaponScalingMultiplier(int scalingLevel) {
        if (scalingLevel <= 0) return 1.0d;
        double perPoint = 0.005d;
        double maxBonus = 0.50d;
        return 1.0d + Math.min(scalingLevel * perPoint, maxBonus);
    }

    public static double getMagicScalingMultiplier(int scalingLevel) {
        if (scalingLevel <= 0) return 1.0d;
        double perPoint = 0.0075d;
        double maxBonus = 0.75d;
        return 1.0d + Math.min(scalingLevel * perPoint, maxBonus);
    }
}