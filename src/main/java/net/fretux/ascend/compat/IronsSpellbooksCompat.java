package net.fretux.ascend.compat;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.util.UUID;

public class IronsSpellbooksCompat {
    private static final UUID INT_MAX_MANA_UUID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID INT_SPELL_POWER_UUID =
            UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID INT_MANA_REGEN_UUID =
            UUID.fromString("33333333-3333-4333-8333-333333333333");
    private static final UUID INT_COOLDOWN_REDUCTION_UUID =
            UUID.fromString("44444444-4444-4444-8444-444444444444");
    private static final UUID CHA_MAX_MANA_UUID =
            UUID.fromString("55555555-5555-4555-8555-555555555555");
    // reserved for future spell-trade hooks
    private static final UUID CHA_SPELL_TRADE_UUID =
            UUID.fromString("66666666-6666-4666-8666-666666666666");
    private static final UUID MAG_SPELL_POWER_UUID =
            UUID.fromString("77777777-7777-4777-8777-777777777777");
    private static final UUID MAG_MANA_REGEN_UUID =
            UUID.fromString("88888888-8888-4888-8888-888888888888");
    private static final UUID MAG_COOLDOWN_REDUCTION_UUID =
            UUID.fromString("99999999-9999-4999-8999-999999999999");
    private static final UUID WILL_MANA_COST_UUID =
            UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID CHA_SUPPORT_POWER_UUID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final UUID CHA_SUMMON_HEALTH_UUID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
    private static final UUID CHA_SUMMON_DAMAGE_UUID =
            UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");


    private IronsSpellbooksCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("irons_spellbooks");
    }

    public static void applyAll(Player player) {
        if (!isLoaded()) return;

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int intelligence = stats.getAttributeLevel("intelligence");
            int charisma = stats.getAttributeLevel("charisma");
            int magicScaling = stats.getAttributeLevel("magic_scaling");
            int willpower = stats.getAttributeLevel("willpower");

            applyIntelligence(player, intelligence);
            applyCharisma(player, charisma); // now also handles support/summons internally
            applyMagicScaling(player, magicScaling);
            applyWillpowerCastEfficiency(player, willpower);
        });
    }

    public static void applyIntelligence(Player player, int intelligence) {
        if (intelligence <= 0) {
            clearIntelligence(player);
            return;
        }
        applyAddModifier(
                player,
                (Attribute) AttributeRegistry.MAX_MANA.get(),
                INT_MAX_MANA_UUID,
                "Ascend INT max mana",
                intelligence * 2.0d
        );
        double spellPowerPerPoint = 0.004d;
        double spellPowerMax = 0.40d;
        double spellPowerBonus = Math.min(intelligence * spellPowerPerPoint, spellPowerMax);
        applyMultiplyBaseModifier(
                player,
                (Attribute) AttributeRegistry.SPELL_POWER.get(),
                INT_SPELL_POWER_UUID,
                "Ascend INT spell power",
                spellPowerBonus
        );
        double regenPerPoint = 0.002d;
        double regenMax = 0.25d;
        double regenBonus = Math.min(intelligence * regenPerPoint, regenMax);
        applyMultiplyBaseModifier(
                player,
                (Attribute) AttributeRegistry.MANA_REGEN.get(),
                INT_MANA_REGEN_UUID,
                "Ascend INT mana regen",
                regenBonus
        );
        double cdrPerPoint = 0.0015d;
        double cdrMax = 0.20d;
        double cdrBonus = Math.min(intelligence * cdrPerPoint, cdrMax);
        applyMultiplyBaseModifier(
                player,
                (Attribute) AttributeRegistry.COOLDOWN_REDUCTION.get(),
                INT_COOLDOWN_REDUCTION_UUID,
                "Ascend INT cooldown reduction",
                cdrBonus
        );
    }

    private static void clearIntelligence(Player player) {
        removeModifier(player, (Attribute) AttributeRegistry.MAX_MANA.get(), INT_MAX_MANA_UUID);
        removeModifier(player, (Attribute) AttributeRegistry.SPELL_POWER.get(), INT_SPELL_POWER_UUID);
        removeModifier(player, (Attribute) AttributeRegistry.MANA_REGEN.get(), INT_MANA_REGEN_UUID);
        removeModifier(player, (Attribute) AttributeRegistry.COOLDOWN_REDUCTION.get(), INT_COOLDOWN_REDUCTION_UUID);
    }

    public static void applyWillpowerCastEfficiency(Player player, int willpower) {
        clearWillpower(player);
        if (willpower <= 0) {
            return;
        }
        double perPoint = 0.0025d;
        double max = 0.25d;
        double reduction = Math.min(willpower * perPoint, max);
        Attribute attr = (Attribute) AttributeRegistry.CAST_TIME_REDUCTION.get();
        if (attr == null) {
            return;
        }
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) {
            return;
        }
        inst.removeModifier(WILL_MANA_COST_UUID);
        if (reduction > 0.0d) {
            inst.addTransientModifier(new AttributeModifier(
                    WILL_MANA_COST_UUID,
                    "Ascend WILL casting time reduction",
                    -reduction,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void clearWillpower(Player player) {
        Attribute attr = (Attribute) AttributeRegistry.CAST_TIME_REDUCTION.get();
        if (attr == null) return;
        removeModifier(player, attr, WILL_MANA_COST_UUID);
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
                (Attribute) AttributeRegistry.MAX_MANA.get(),
                CHA_MAX_MANA_UUID,
                "Ascend CHA max mana",
                bonusMana
        );
        double supportPerPoint = 0.003d;
        double supportMax = 0.30d;
        double supportBonus = Math.min(charisma * supportPerPoint, supportMax);
        Attribute supportAttr = null;
        if (supportAttr != null && supportBonus > 0.0d) {
            applyMultiplyBaseModifier(
                    player,
                    supportAttr,
                    CHA_SUPPORT_POWER_UUID,
                    "Ascend CHA support spell power",
                    supportBonus
            );
        }
        double minionHpPerPoint = 0.004d;
        double minionDmgPerPoint = 0.003d;
        double minionHpBonus = Math.min(charisma * minionHpPerPoint, 0.40d);
        double minionDmgBonus = Math.min(charisma * minionDmgPerPoint, 0.30d);
        Attribute summonHpAttr = null;
        Attribute summonDmgAttr = null;
        if (summonHpAttr != null && minionHpBonus > 0.0d) {
            applyMultiplyBaseModifier(
                    player,
                    summonHpAttr,
                    CHA_SUMMON_HEALTH_UUID,
                    "Ascend CHA summon max health",
                    minionHpBonus
            );
        }
        if (summonDmgAttr != null && minionDmgBonus > 0.0d) {
            applyMultiplyBaseModifier(
                    player,
                    summonDmgAttr,
                    CHA_SUMMON_DAMAGE_UUID,
                    "Ascend CHA summon damage",
                    minionDmgBonus
            );
        }
    }

    private static void clearCharisma(Player player) {
        removeModifier(player, (Attribute) AttributeRegistry.MAX_MANA.get(), CHA_MAX_MANA_UUID);
        Attribute supportAttr = null;
        Attribute summonHpAttr = null;
        Attribute summonDmgAttr = null;
        if (supportAttr != null) {
            removeModifier(player, supportAttr, CHA_SUPPORT_POWER_UUID);
        }
        if (summonHpAttr != null) {
            removeModifier(player, summonHpAttr, CHA_SUMMON_HEALTH_UUID);
        }
        if (summonDmgAttr != null) {
            removeModifier(player, summonDmgAttr, CHA_SUMMON_DAMAGE_UUID);
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
                (Attribute) AttributeRegistry.SPELL_POWER.get(),
                MAG_SPELL_POWER_UUID,
                "Ascend Magic Scaling spell power",
                spBonus
        );
        double regenPerPoint = 0.001d;
        double regenMax = 0.15d;
        double regenBonus = Math.min(magicScaling * regenPerPoint, regenMax);
        applyMultiplyBaseModifier(
                player,
                (Attribute) AttributeRegistry.MANA_REGEN.get(),
                MAG_MANA_REGEN_UUID,
                "Ascend Magic Scaling mana regen",
                regenBonus
        );

        double cdrPerPoint = 0.001d;
        double cdrMax = 0.10d;
        double cdrBonus = Math.min(magicScaling * cdrPerPoint, cdrMax);
        applyMultiplyBaseModifier(
                player,
                (Attribute) AttributeRegistry.COOLDOWN_REDUCTION.get(),
                MAG_COOLDOWN_REDUCTION_UUID,
                "Ascend Magic Scaling cooldown reduction",
                cdrBonus
        );
    }

    private static void clearMagicScaling(Player player) {
        removeModifier(player, (Attribute) AttributeRegistry.SPELL_POWER.get(), MAG_SPELL_POWER_UUID);
        removeModifier(player, (Attribute) AttributeRegistry.MANA_REGEN.get(), MAG_MANA_REGEN_UUID);
        removeModifier(player, (Attribute) AttributeRegistry.COOLDOWN_REDUCTION.get(), MAG_COOLDOWN_REDUCTION_UUID);
    }

    private static void applyAddModifier(Player player, Attribute attribute, UUID id, String name, double value) {
        if (attribute == null) return;
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0.0d) {
            inst.addTransientModifier(new AttributeModifier(
                    id,
                    name,
                    value,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    private static void applyMultiplyBaseModifier(Player player, Attribute attribute, UUID id, String name, double value) {
        if (attribute == null) return;
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0.0d) {
            inst.addTransientModifier(new AttributeModifier(
                    id,
                    name,
                    value,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void removeModifier(Player player, Attribute attribute, UUID id) {
        if (attribute == null) return;
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst != null) {
            inst.removeModifier(id);
        }
    }
}