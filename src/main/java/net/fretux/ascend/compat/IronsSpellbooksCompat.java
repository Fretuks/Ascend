
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
                (Holder<Attribute>) AttributeRegistry.MAX_MANA.get(),
                INT_MAX_MANA_ID,
                intelligence * 2.0d
        );
        double spellPowerPerPoint = 0.004d;
        double spellPowerMax = 0.40d;
        double spellPowerBonus = Math.min(intelligence * spellPowerPerPoint, spellPowerMax);
        applyMultiplyBaseModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.SPELL_POWER.get(),
                INT_SPELL_POWER_ID,
                spellPowerBonus
        );
        double regenPerPoint = 0.002d;
        double regenMax = 0.25d;
        double regenBonus = Math.min(intelligence * regenPerPoint, regenMax);
        applyMultiplyBaseModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.MANA_REGEN.get(),
                INT_MANA_REGEN_ID,
                regenBonus
        );
        double cdrPerPoint = 0.0015d;
        double cdrMax = 0.20d;
        double cdrBonus = Math.min(intelligence * cdrPerPoint, cdrMax);
        applyMultiplyBaseModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.COOLDOWN_REDUCTION.get(),
                INT_COOLDOWN_REDUCTION_ID,
                cdrBonus
        );
    }

    private static void clearIntelligence(Player player) {
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.MAX_MANA.get(), INT_MAX_MANA_ID);
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.SPELL_POWER.get(), INT_SPELL_POWER_ID);
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.MANA_REGEN.get(), INT_MANA_REGEN_ID);
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.COOLDOWN_REDUCTION.get(), INT_COOLDOWN_REDUCTION_ID);
    }

    public static void applyWillpowerCastEfficiency(Player player, int willpower) {
        clearWillpower(player);
        if (willpower <= 0) {
            return;
        }
        double perPoint = 0.0025d;
        double max = 0.25d;
        double reduction = Math.min(willpower * perPoint, max);
        Holder<Attribute> attrHolder = (Holder<Attribute>) AttributeRegistry.CAST_TIME_REDUCTION.get();
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
        Holder<Attribute> attrHolder = (Holder<Attribute>) AttributeRegistry.CAST_TIME_REDUCTION.get();
        if (attrHolder == null) return;
        removeModifier(player, attrHolder, WILL_MANA_COST_ID);
    }

    public static void applyCharisma(Player player, int charisma) {
        clearCharisma(player);
        if (charisma <= 0) {
            return;
        }
        double manaPerPoint = 0.5d;
        double bonusMana = charisma * manaPerPoint;
        applyAddModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.MAX_MANA.get(),
                CHA_MAX_MANA_ID,
                bonusMana
        );
        double supportPerPoint = 0.003d;
        double supportMax = 0.30d;
        double supportBonus = Math.min(charisma * supportPerPoint, supportMax);
        Holder<Attribute> supportAttr = (Holder<Attribute>) AttributeRegistry.HOLY_SPELL_POWER.get();
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
        Holder<Attribute> summonDmgAttr = (Holder<Attribute>) AttributeRegistry.SUMMON_DAMAGE.get();
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
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.MAX_MANA.get(), CHA_MAX_MANA_ID);
        Holder<Attribute> supportAttr = (Holder<Attribute>) AttributeRegistry.HOLY_SPELL_POWER.get();
        Holder<Attribute> summonHpAttr = null;
        Holder<Attribute> summonDmgAttr = (Holder<Attribute>) AttributeRegistry.SUMMON_DAMAGE.get();
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
        double spPerPoint = 0.006d;
        double spMax = 0.60d;
        double spBonus = Math.min(magicScaling * spPerPoint, spMax);
        applyMultiplyBaseModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.SPELL_POWER.get(),
                MAG_SPELL_POWER_ID,
                spBonus
        );
        double regenPerPoint = 0.001d;
        double regenMax = 0.15d;
        double regenBonus = Math.min(magicScaling * regenPerPoint, regenMax);
        applyMultiplyBaseModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.MANA_REGEN.get(),
                MAG_MANA_REGEN_ID,
                regenBonus
        );

        double cdrPerPoint = 0.001d;
        double cdrMax = 0.10d;
        double cdrBonus = Math.min(magicScaling * cdrPerPoint, cdrMax);
        applyMultiplyBaseModifier(
                player,
                (Holder<Attribute>) AttributeRegistry.COOLDOWN_REDUCTION.get(),
                MAG_COOLDOWN_REDUCTION_ID,
                cdrBonus
        );
    }

    private static void clearMagicScaling(Player player) {
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.SPELL_POWER.get(), MAG_SPELL_POWER_ID);
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.MANA_REGEN.get(), MAG_MANA_REGEN_ID);
        removeModifier(player, (Holder<Attribute>) AttributeRegistry.COOLDOWN_REDUCTION.get(), MAG_COOLDOWN_REDUCTION_ID);
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