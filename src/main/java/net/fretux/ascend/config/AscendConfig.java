package net.fretux.ascend.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AscendConfig {

    public static class Common {
        public final ForgeConfigSpec.IntValue xpPerMobKill;
        public final ForgeConfigSpec.IntValue xpPerBlockBreak;
        public final ForgeConfigSpec.IntValue xpPerCraft;
        public final ForgeConfigSpec.IntValue xpPerSmelt;
        public final ForgeConfigSpec.IntValue xpPerTrade;
        public final ForgeConfigSpec.IntValue xpPerDamageTaken;
        public final ForgeConfigSpec.IntValue xpPerMovement;
        public final ForgeConfigSpec.IntValue xpPerPotion;
        public final ForgeConfigSpec.IntValue pointsPerLevel;
        public final ForgeConfigSpec.IntValue maxAscendLevel;
        public final ForgeConfigSpec.IntValue maxAttributePoints;

        public final ForgeConfigSpec.DoubleValue xpMultiplier;
        public final ForgeConfigSpec.DoubleValue attributeScalingMultiplier;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("Ascend Progression");

            xpPerMobKill = builder.comment("Ascend XP gained for killing a mob.")
                    .defineInRange("xpPerMobKill", 5, 0, 1000);
            xpPerBlockBreak = builder.comment("Ascend XP gained for breaking a block (ore/stone/log).")
                    .defineInRange("xpPerBlockBreak", 2, 0, 1000);
            xpPerCraft = builder.comment("Ascend XP gained for crafting items.")
                    .defineInRange("xpPerCraft", 3, 0, 1000);
            xpPerSmelt = builder.comment("Ascend XP gained for smelting items.")
                    .defineInRange("xpPerSmelt", 2, 0, 1000);
            xpPerTrade = builder.comment("Ascend XP gained for trading with villagers.")
                    .defineInRange("xpPerTrade", 5, 0, 1000);
            xpPerDamageTaken = builder.comment("Ascend XP gained for taking damage.")
                    .defineInRange("xpPerDamageTaken", 2, 0, 1000);
            xpPerMovement = builder.comment("Ascend XP gained for movement actions.")
                    .defineInRange("xpPerMovement", 1, 0, 1000);
            xpPerPotion = builder.comment("Ascend XP gained for using a potion.")
                    .defineInRange("xpPerPotion", 3, 0, 1000);

            pointsPerLevel = builder.comment("Unspent points gained per Ascend level.")
                    .defineInRange("pointsPerLevel", 15, 1, 1000);
            maxAscendLevel = builder.comment("Maximum Ascend level.")
                    .defineInRange("maxAscendLevel", 20, 1, 100);
            maxAttributePoints = builder.comment("Maximum points in a single attribute.")
                    .defineInRange("maxAttributePoints", 100, 1, 1000);

            xpMultiplier = builder.comment("Global multiplier for all Ascend XP gain.")
                    .defineInRange("xpMultiplier", 1.0D, 0.0D, 100.0D);
            attributeScalingMultiplier = builder.comment("Global multiplier for all attribute-based stat bonuses (damage, mana, speed, etc.).")
                    .defineInRange("attributeScalingMultiplier", 1.0D, 0.1D, 10.0D);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }
}
