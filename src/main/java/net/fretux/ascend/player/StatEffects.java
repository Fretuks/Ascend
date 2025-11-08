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
    private static final UUID AGILITY_SPEED_UUID =
            UUID.fromString("c12e9b6f-1d79-4a49-b57f-7f563c10aa11");
    private static final UUID AGILITY_STEALTH_UUID =
            UUID.fromString("d4e8a9f2-3b5c-4d0a-9f11-8acb3f0b77e2");

    private StatEffects() {}

    public static void applyAll(Player player) {
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            applyStrength(player, stats.getAttributeLevel("strength"));
            applyAgility(player, stats.getAttributeLevel("agility"));
            // later: fortitude, etc.
        });
    }

    public static void applyStrength(Player player, int strengthLevel) {
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
    
    public static void applyAgility(Player player, int agilityLevel) {
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            move.removeModifier(AGILITY_SPEED_UUID);
            if (agilityLevel > 0) {
                double speedBonus = agilityLevel * 0.0005d;
                move.addTransientModifier(new AttributeModifier(
                        AGILITY_SPEED_UUID,
                        "Ascend Agility speed bonus",
                        speedBonus,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
        AttributeInstance follow = player.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) {
            follow.removeModifier(AGILITY_STEALTH_UUID);
            if (agilityLevel > 0) {
                double perPoint = 0.003d;          
                double maxReduction = 0.5d;      
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
}
