package net.fretux.ascend.player;

import net.fretux.ascend.compat.IronsSpellbooksCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

import static net.fretux.ascend.compat.IronsSpellbooksCompat.applyCharisma;
import static net.fretux.ascend.compat.IronsSpellbooksCompat.applyIntelligence;

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

    private StatEffects() {}
    
    public static void applyAll(Player player) {
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            applyStrength(player, stats.getAttributeLevel("strength"));
            applyAgility(player, stats.getAttributeLevel("agility"));
            applyFortitude(player, stats.getAttributeLevel("fortitude"));
            // - Willpower: see getWillpowerSanityMultiplier / getWillpowerTempoMultiplier.
            if (IronsSpellbooksCompat.isLoaded()) {
                IronsSpellbooksCompat.applyAll(player);
            }
        });
    }

    /**
     * Strength:
     *  - +0.1 attack damage per point (0.05 hearts)
     *  - +0.03 knockback per point
     *  - Armor bypass handled in your combat event using strength level.
     */
    public static void applyStrength(Player player, int strengthLevel) {
        // Damage
        AttributeInstance atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(STRENGTH_DAMAGE_UUID);
            if (strengthLevel > 0) {
                double dmgBonus = strengthLevel * 0.1d;
                atk.addTransientModifier(new AttributeModifier(
                        STRENGTH_DAMAGE_UUID,
                        "Ascend Strength damage bonus",
                        dmgBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        // Knockback
        AttributeInstance kb = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (kb != null) {
            kb.removeModifier(STRENGTH_KNOCKBACK_UUID);
            if (strengthLevel > 0) {
                double kbBonus = strengthLevel * 0.03d;
                kb.addTransientModifier(new AttributeModifier(
                        STRENGTH_KNOCKBACK_UUID,
                        "Ascend Strength knockback bonus",
                        kbBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    /**
     * Agility:
     *  - Small movement speed bonus per point.
     *  - Stealth handled externally (e.g. other mods can read agility).
     */
    public static void applyAgility(Player player, int agilityLevel) {
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            move.removeModifier(AGILITY_SPEED_UUID);
            if (agilityLevel > 0) {
                // Example: +0.002 per point = +0.2 at 100 (about +20% on base 0.1)
                double bonus = agilityLevel * 0.002d;
                move.addTransientModifier(new AttributeModifier(
                        AGILITY_SPEED_UUID,
                        "Ascend Agility speed bonus",
                        bonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    /**
     * Fortitude:
     *  - Max HP up per point.
     *  - Knockback taken reduced via attribute.
     *  (Poison/hunger/debuff tuning handled via your other event hooks.)
     */
    public static void applyFortitude(Player player, int fortitudeLevel) {
        // Max health
        AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(FORTITUDE_HEALTH_UUID);
            if (fortitudeLevel > 0) {
                // Example: 0.25 HP (1/8 heart) per point = +12.5 hearts at 100
                double healthBonus = fortitudeLevel * 0.25d;
                hp.addTransientModifier(new AttributeModifier(
                        FORTITUDE_HEALTH_UUID,
                        "Ascend Fortitude health bonus",
                        healthBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        // Knockback resistance
        AttributeInstance kbRes = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null) {
            kbRes.removeModifier(FORTITUDE_KB_RESIST_UUID);
            if (fortitudeLevel > 0) {
                // Tiny scaling; cap at 60% here
                double res = Math.min(0.006d * fortitudeLevel, 0.60d);
                kbRes.addTransientModifier(new AttributeModifier(
                        FORTITUDE_KB_RESIST_UUID,
                        "Ascend Fortitude knockback resist",
                        res,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    /**
     * Intelligence:
     *  - Used for mana & potion/enchant effects via other hooks.
     *  - Here we only expose a mana bonus helper.
     */

    // ===== Willpower external API =====

    /**
     * Sanity drain multiplier from Willpower.
     * Other mods can call this directly.
     *
     *  - Starts at 1.0
     *  - Reduces up to 50% at high Willpower.
     */
    public static float getWillpowerSanityMultiplier(int willpowerLevel) {
        if (willpowerLevel <= 0) return 1.0f;
        double reduction = Math.min(willpowerLevel * 0.005d, 0.5d); // up to -50%
        return (float) (1.0d - reduction);
    }

    /**
     * Tempo gain/multiplier from Willpower.
     * Other mods can plug this into stamina/tempo/sprint systems.
     *
     *  - Starts at 1.0
     *  - Increases up to +40%.
     */
    public static float getWillpowerTempoMultiplier(int willpowerLevel) {
        if (willpowerLevel <= 0) return 1.0f;
        double bonus = Math.min(willpowerLevel * 0.004d, 0.4d); // up to +40%
        return (float) (1.0d + bonus);
    }

    /** Convenience: from player capability → sanity multiplier. */
    public static float getPlayerSanityMultiplier(Player player) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                .map(stats -> getWillpowerSanityMultiplier(stats.getAttributeLevel("willpower")))
                .orElse(1.0f);
    }

    /** Convenience: from player capability → tempo multiplier. */
    public static float getPlayerTempoMultiplier(Player player) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                .map(stats -> getWillpowerTempoMultiplier(stats.getAttributeLevel("willpower")))
                .orElse(1.0f);
    }

    // ===== Intelligence external helper (for your mana system) =====

    /**
     * Example mana bonus: 1 mana per INT, capped by caller if needed.
     */
    public static int getIntelligenceManaBonus(int intelligenceLevel) {
        return Math.max(0, intelligenceLevel);
    }

    // ===== Charisma helper (used in trade discount handler) =====

    /**
     * Charisma → trade discount fraction.
     * Returns value in [0, max], e.g. 0.0 = no discount, 0.25 = 25% cheaper.
     */
    public static double getCharismaTradeDiscount(int charismaLevel) {
        if (charismaLevel <= 0) return 0.0d;
        double perPoint = 0.01d;  
        double max = 0.30d;        
        return Math.min(charismaLevel * perPoint, max);
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