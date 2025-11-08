package net.fretux.ascend.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class StatEffects {

    // ===== Strength UUIDs =====
    private static final UUID STRENGTH_DAMAGE_UUID =
            UUID.fromString("a7b8b2c3-9b0d-4e1a-8ee3-21f6a7b2c3d4");
    private static final UUID STRENGTH_KNOCKBACK_UUID =
            UUID.fromString("b17f0d74-9cd9-4f90-9ec8-6a1046e2f912");

    // ===== Agility UUIDs =====
    private static final UUID AGILITY_SPEED_UUID =
            UUID.fromString("c12e9b6f-1d79-4a49-b57f-7f563c10aa11");
    private static final UUID AGILITY_STEALTH_UUID =
            UUID.fromString("d4e8a9f2-3b5c-4d0a-9f11-8acb3f0b77e2");

    // ===== Fortitude UUIDs =====
    private static final UUID FORTITUDE_HEALTH_UUID =
            UUID.fromString("e2b8ff31-2c3d-4a60-9ce4-12aa4d3f9b77");
    private static final UUID FORTITUDE_KB_RESIST_UUID =
            UUID.fromString("f4c1a9d2-6b8e-4f01-8a21-9e7c5b3d12aa");

    private StatEffects() {}

    /** Call on login/clone and after stat changes. */
    public static void applyAll(Player player) {
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            applyStrength(player, stats.getAttributeLevel("strength"));
            applyAgility(player, stats.getAttributeLevel("agility"));
            applyFortitude(player, stats.getAttributeLevel("fortitude"));
            applyIntelligence(player, stats.getAttributeLevel("intelligence"));
        });
    }

    // ========== STRENGTH ==========

    public static void applyStrength(Player player, int strengthLevel) {
        AttributeInstance atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(STRENGTH_DAMAGE_UUID);
            if (strengthLevel > 0) {
                double dmgBonus = strengthLevel * 0.1d; // 0.05 hearts per point
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

    // ========== AGILITY ==========

    public static void applyAgility(Player player, int agilityLevel) {
        // Movement speed
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            move.removeModifier(AGILITY_SPEED_UUID);
            if (agilityLevel > 0) {
                // ~+2.5% at 10, +25% at 100
                double speedBonus = agilityLevel * 0.00025d;
                move.addTransientModifier(new AttributeModifier(
                        AGILITY_SPEED_UUID,
                        "Ascend Agility speed bonus",
                        speedBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        // Stealth via follow range reduction
        AttributeInstance follow = player.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) {
            follow.removeModifier(AGILITY_STEALTH_UUID);
            if (agilityLevel > 0) {
                double perPoint = 0.003d;   // 0.3% per point
                double maxReduction = 0.5d; // up to -50%
                double reduction = Math.min(agilityLevel * perPoint, maxReduction);

                if (reduction > 0.0d) {
                    double multiplier = 1.0d - reduction;
                    follow.addTransientModifier(new AttributeModifier(
                            AGILITY_STEALTH_UUID,
                            "Ascend Agility stealth bonus",
                            multiplier - 1.0d,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    ));
                }
            }
        }
    }

    // ========== FORTITUDE ==========

    public static void applyFortitude(Player player, int fortitudeLevel) {
        // Max HP
        AttributeInstance maxHp = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHp != null) {
            maxHp.removeModifier(FORTITUDE_HEALTH_UUID);
            if (fortitudeLevel > 0) {
                // +0.2 HP per point (1 heart per 10 points)
                double bonusHp = fortitudeLevel * 0.2d;
                maxHp.addTransientModifier(new AttributeModifier(
                        FORTITUDE_HEALTH_UUID,
                        "Ascend Fortitude health bonus",
                        bonusHp,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }

        float maxHealthNow = player.getMaxHealth();
        if (player.getHealth() > maxHealthNow) {
            player.setHealth(maxHealthNow);
        }

        // Knockback resistance
        AttributeInstance kbRes = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null) {
            kbRes.removeModifier(FORTITUDE_KB_RESIST_UUID);
            if (fortitudeLevel > 0) {
                // 0.3% per point, up to 30% at 100 (with safety cap)
                double bonus = fortitudeLevel * 0.003d;
                double safeBonus = Math.min(bonus, 0.8d);
                if (safeBonus > 0) {
                    kbRes.addTransientModifier(new AttributeModifier(
                            FORTITUDE_KB_RESIST_UUID,
                            "Ascend Fortitude knockback resist",
                            safeBonus,
                            AttributeModifier.Operation.ADDITION
                    ));
                }
            }
        }
    }

    // ========== INTELLIGENCE ==========

    /**
     * Intelligence:
     *  - Max mana is exposed via PlayerStats (see below).
     *  - Potions & enchanting are handled via event hooks.
     *
     * This exists for future attribute-based hooks if needed.
     */
    public static void applyIntelligence(Player player, int intelligenceLevel) {
        // No direct vanilla attribute to touch here for mana.
        // Potion & enchanting effects are implemented in event handlers.
    }
}