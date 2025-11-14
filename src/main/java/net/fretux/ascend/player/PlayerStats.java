package net.fretux.ascend.player;

import net.fretux.ascend.config.AscendConfig;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    public static final int POINTS_PER_LEVEL = AscendConfig.COMMON.pointsPerLevel.get();
    public static final int MAX_ASCEND_LEVEL = AscendConfig.COMMON.maxAscendLevel.get();
    public static final int MAX_ATTRIBUTE_POINTS = AscendConfig.COMMON.maxAttributePoints.get();
    private final Map<String, Integer> attributes = new HashMap<>();
    private int ascendLevel = 1;
    private int ascendXP = 0;
    private int unspentPoints = POINTS_PER_LEVEL;

    public PlayerStats() {
        for (String attr : new String[]{
                "strength", "agility", "fortitude", "intelligence",
                "willpower", "charisma",
                "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"}) {
            attributes.put(attr, 0);
        }
    }

    public void addAscendXP(int amount) {
        if (amount <= 0 || ascendLevel >= MAX_ASCEND_LEVEL) return;
        ascendXP += amount;
        while (ascendLevel < MAX_ASCEND_LEVEL) {
            int xpNeeded = getXPToNextAscendLevel();
            if (ascendXP < xpNeeded) break;
            ascendXP -= xpNeeded;
            ascendLevel++;
            unspentPoints += POINTS_PER_LEVEL;
            knowledge += 1;
        }
        if (ascendLevel >= MAX_ASCEND_LEVEL) {
            int xpNeeded = getXPToNextAscendLevel();
            if (ascendXP > xpNeeded) {
                ascendXP = xpNeeded;
            }
        }
    }


    public int getAscendLevel() {
        return ascendLevel;
    }

    public int getAscendXP() {
        return ascendXP;
    }

    public int getXPToNextAscendLevel() {
        if (ascendLevel >= MAX_ASCEND_LEVEL) return 0;
        return 100 * ascendLevel;
    }

    public int getUnspentPoints() {
        return unspentPoints;
    }

    public void addUnspentPoints(int amount) {
        unspentPoints += amount;
    }

    public boolean spendPoints(String attribute) {
        if (!attributes.containsKey(attribute)) return false;
        int level = attributes.get(attribute);
        if (level >= MAX_ATTRIBUTE_POINTS) {
            return false;
        }
        if (unspentPoints <= 0) {
            return false;
        }
        unspentPoints -= 1;
        attributes.put(attribute, level + 1);
        return true;
    }

    public int getCostToUpgrade(String attribute) {
        return 1;
    }

    public int getAttributeLevel(String attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    public void setAttributeLevel(String attribute, int level) {
        if (!attributes.containsKey(attribute)) return;
        int clamped = Math.max(0, Math.min(level, MAX_ATTRIBUTE_POINTS));
        attributes.put(attribute, clamped);
    }

    public int getMaxMana() {
        int intelligence = getAttributeLevel("intelligence");
        int base = 50;
        int perPoint = 3;
        return base + intelligence * perPoint;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("AscendLevel", ascendLevel);
        tag.putInt("AscendXP", ascendXP);
        tag.putInt("UnspentPoints", unspentPoints);
        tag.putInt("Knowledge", knowledge);
        tag.putBoolean("HasUsedMoonseye", hasUsedMoonseye);
        CompoundTag attrTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            attrTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("Attributes", attrTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        ascendLevel = tag.contains("AscendLevel") ? tag.getInt("AscendLevel") : 1;
        ascendXP = tag.contains("AscendXP") ? tag.getInt("AscendXP") : 0;
        if (tag.contains("UnspentPoints")) {
            unspentPoints = tag.getInt("UnspentPoints");
        } else {
            unspentPoints = POINTS_PER_LEVEL;
        }
        if (tag.contains("Attributes")) {
            CompoundTag attrTag = tag.getCompound("Attributes");
            for (String key : attributes.keySet()) {
                int loaded = attrTag.getInt(key);
                attributes.put(key, Math.max(0, Math.min(loaded, MAX_ATTRIBUTE_POINTS)));
            }
        }
        if (tag.contains("Knowledge")) {
            knowledge = tag.getInt("Knowledge");
        }
        if (tag.contains("HasUsedMoonseye")) {
            hasUsedMoonseye = tag.getBoolean("HasUsedMoonseye");
        }
    }

    private int knowledge = 0;
    private boolean hasUsedMoonseye = false;

    public int getKnowledge() {
        return knowledge;
    }

    public void addKnowledge(int amount) {
        knowledge = Math.max(0, knowledge + amount);
    }

    public boolean hasUsedMoonseye(){
        return hasUsedMoonseye;
    }

    public void setHasUsedMoonseye(boolean used) {
        this.hasUsedMoonseye = used;
    }

    public void refundAllPoints() {
        int totalSpent = 0;
        for (String key : attributes.keySet()) {
            int lvl = attributes.get(key);
            totalSpent += lvl;
            attributes.put(key, 0);
        }
        unspentPoints += totalSpent;
    }

    public static class Serializer implements IAttachmentSerializer<CompoundTag, PlayerStats> {
        @Override
        public PlayerStats read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
            PlayerStats stats = new PlayerStats();
            stats.deserializeNBT(tag);
            return stats;
        }

        @Override
        public CompoundTag write(PlayerStats stats, net.minecraft.core.HolderLookup.Provider provider) {
            return stats.serializeNBT();
        }
    }
}