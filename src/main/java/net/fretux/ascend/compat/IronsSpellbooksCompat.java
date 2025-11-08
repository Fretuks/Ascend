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

    private IronsSpellbooksCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("irons_spellbooks");
    }
    
    public static void applyAll(Player player) {
        if (!isLoaded()) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            applyIntelligence(player, stats.getAttributeLevel("intelligence"));
            applyCharisma(player, stats.getAttributeLevel("charisma"));
            applyMagicScaling(player, stats.getAttributeLevel("magic_scaling"));
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
    
    public static void applyCharisma(Player player, int charisma) {
        if (charisma <= 0) {
            clearCharisma(player);
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
        // Trade / reputation hooks can use CHA_SPELL_TRADE_UUID later.
    }

    private static void clearCharisma(Player player) {
        removeModifier(player, (Attribute) AttributeRegistry.MAX_MANA.get(), CHA_MAX_MANA_UUID);
        // Any future CHA ISS modifiers get cleared here.
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