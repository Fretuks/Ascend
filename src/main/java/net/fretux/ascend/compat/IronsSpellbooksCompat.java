
package net.fretux.ascend.compat;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public class IronsSpellbooksCompat {
    private static final ResourceLocation INT_MAX_MANA_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "int_max_mana");
    private static final ResourceLocation INT_SPELL_POWER_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "int_spell_power");
    private static final ResourceLocation INT_MANA_REGEN_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "int_mana_regen");
    private static final ResourceLocation INT_COOLDOWN_REDUCTION_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "int_cooldown_reduction");
    private static final ResourceLocation CHA_MAX_MANA_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "cha_max_mana");
    private static final ResourceLocation CHA_SPELL_TRADE_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "cha_spell_trade");
    private static final ResourceLocation MAG_SPELL_POWER_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "mag_spell_power");
    private static final ResourceLocation MAG_MANA_REGEN_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "mag_mana_regen");
    private static final ResourceLocation MAG_COOLDOWN_REDUCTION_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "mag_cooldown_reduction");
    private static final ResourceLocation WILL_MANA_COST_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "will_cast_time");
    private static final ResourceLocation CHA_SUPPORT_POWER_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "cha_support_power");
    private static final ResourceLocation CHA_SUMMON_HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "cha_summon_health");
    private static final ResourceLocation CHA_SUMMON_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath("ascend", "cha_summon_damage");


    private IronsSpellbooksCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("irons_spellbooks");
    }

    public static void applyAll(Player player) {
        if (!isLoaded()) return;

        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        int intelligence = stats.getAttributeLevel("intelligence");
        int charisma = stats.getAttributeLevel("charisma");
        int magicScaling = stats.getAttributeLevel("magic_scaling");
        int willpower = stats.getAttributeLevel("willpower");

        applyIntelligence(player, intelligence);
        applyCharisma(player, charisma);
        applyMagicScaling(player, magicScaling);
        applyWillpowerCastEfficiency(player, willpower);
    }

    public static void applyIntelligence(Player player, int intelligence) {
        if (intelligence <= 0) {
            clearIntelligence(player);
            return;
        }
        applyAddModifier(
                player,
                AttributeRegistry.MAX_MANA,
                INT_MAX_MANA_ID,
                intelligence * 3.0d
        );
        double regenPerPoint = 0.004d;
        double regenMax = 0.40d;
        double regenBonus = Math.min(intelligence * regenPerPoint, regenMax);
        applyMultiplyBaseModifier(
                player,
                AttributeRegistry.MANA_REGEN,
                INT_MANA_REGEN_ID,
                regenBonus
        );
    }

    private static void clearIntelligence(Player player) {
        removeModifier(player, AttributeRegistry.MAX_MANA, INT_MAX_MANA_ID);
        removeModifier(player, AttributeRegistry.MANA_REGEN, INT_MANA_REGEN_ID);
    }

    public static void applyWillpowerCastEfficiency(Player player, int willpower) {
        clearWillpower(player);
        if (willpower <= 0) {
            return;
        }
        double perPoint = 0.0025d;
        double max = 0.25d;
        double reduction = Math.min(willpower * perPoint, max);
        Holder<Attribute> attrHolder = AttributeRegistry.CAST_TIME_REDUCTION;
        if (attrHolder == null) {
            return;
        }
        AttributeInstance inst = player.getAttribute(attrHolder);
        if (inst == null) {
            return;
        }
        inst.removeModifier(WILL_MANA_COST_ID);
        if (reduction > 0.0d) {
            inst.addTransientModifier(new AttributeModifier(
                    WILL_MANA_COST_ID,
                    -reduction,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    private static void clearWillpower(Player player) {
        Holder<Attribute> attrHolder = AttributeRegistry.CAST_TIME_REDUCTION;
        if (attrHolder == null) return;
        removeModifier(player, attrHolder, WILL_MANA_COST_ID);
    }

    public static void applyCharisma(Player player, int charisma) {
        clearCharisma(player);
        if (charisma <= 0) {
            return;
        }
        double manaPerPoint = 1.5d;
        double bonusMana = charisma * manaPerPoint;
        applyAddModifier(
                player,
                AttributeRegistry.MAX_MANA,
                CHA_MAX_MANA_ID,
                bonusMana
        );
        double supportPerPoint = 0.003d;
        double supportMax = 0.30d;
        double supportBonus = Math.min(charisma * supportPerPoint, supportMax);
        Holder<Attribute> supportAttr = AttributeRegistry.HOLY_SPELL_POWER;
        if (supportAttr != null && supportBonus > 0.0d) {
            applyMultiplyBaseModifier(
                    player,
                    supportAttr,
                    CHA_SUPPORT_POWER_ID,
                    supportBonus
            );
        }
        double minionHpPerPoint = 0.004d;
        double minionDmgPerPoint = 0.003d;
        double minionHpBonus = Math.min(charisma * minionHpPerPoint, 0.40d);
        double minionDmgBonus = Math.min(charisma * minionDmgPerPoint, 0.30d);
        Holder<Attribute> summonHpAttr = null;
        Holder<Attribute> summonDmgAttr = AttributeRegistry.SUMMON_DAMAGE;
        if (summonHpAttr != null && minionHpBonus > 0.0d) {
            applyMultiplyBaseModifier(
                    player,
                    summonHpAttr,
                    CHA_SUMMON_HEALTH_ID,
                    minionHpBonus
            );
        }
        if (summonDmgAttr != null && minionDmgBonus > 0.0d) {
            applyMultiplyBaseModifier(
                    player,
                    summonDmgAttr,
                    CHA_SUMMON_DAMAGE_ID,
                    minionDmgBonus
            );
        }
    }

    private static void clearCharisma(Player player) {
        removeModifier(player, AttributeRegistry.MAX_MANA, CHA_MAX_MANA_ID);
        Holder<Attribute> supportAttr = AttributeRegistry.HOLY_SPELL_POWER;
        Holder<Attribute> summonHpAttr = null;
        Holder<Attribute> summonDmgAttr = AttributeRegistry.SUMMON_DAMAGE;
        if (supportAttr != null) {
            removeModifier(player, supportAttr, CHA_SUPPORT_POWER_ID);
        }
        if (summonHpAttr != null) {
            removeModifier(player, summonHpAttr, CHA_SUMMON_HEALTH_ID);
        }
        if (summonDmgAttr != null) {
            removeModifier(player, summonDmgAttr, CHA_SUMMON_DAMAGE_ID);
        }
    }

    public static void applyMagicScaling(Player player, int magicScaling) {
        if (magicScaling <= 0) {
            clearMagicScaling(player);
            return;
        }
        double spPerPoint = 0.0075d;
        double spMax = 0.75d;
        double spBonus = Math.min(magicScaling * spPerPoint, spMax);
        applyMultiplyBaseModifier(
                player,
                AttributeRegistry.SPELL_POWER,
                MAG_SPELL_POWER_ID,
                spBonus
        );

        double cdrPerPoint = 0.0012d;
        double cdrMax = 0.18d;
        double cdrBonus = Math.min(magicScaling * cdrPerPoint, cdrMax);
        applyMultiplyBaseModifier(
                player,
                AttributeRegistry.COOLDOWN_REDUCTION,
                MAG_COOLDOWN_REDUCTION_ID,
                cdrBonus
        );
    }

    private static void clearMagicScaling(Player player) {
        removeModifier(player, AttributeRegistry.SPELL_POWER, MAG_SPELL_POWER_ID);
        removeModifier(player, AttributeRegistry.COOLDOWN_REDUCTION, MAG_COOLDOWN_REDUCTION_ID);
    }

    private static void applyAddModifier(Player player, Holder<Attribute> attributeHolder, ResourceLocation id, double value) {
        if (attributeHolder == null) return;
        AttributeInstance inst = player.getAttribute(attributeHolder);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0.0d) {
            inst.addTransientModifier(new AttributeModifier(
                    id,
                    value,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    private static void applyMultiplyBaseModifier(Player player, Holder<Attribute> attributeHolder, ResourceLocation id, double value) {
        if (attributeHolder == null) return;
        AttributeInstance inst = player.getAttribute(attributeHolder);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0.0d) {
            inst.addTransientModifier(new AttributeModifier(
                    id,
                    value,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    private static void removeModifier(Player player, Holder<Attribute> attributeHolder, ResourceLocation id) {
        if (attributeHolder == null) return;
        AttributeInstance inst = player.getAttribute(attributeHolder);
        if (inst != null) {
            inst.removeModifier(id);
        }
    }
}