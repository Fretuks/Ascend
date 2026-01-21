package net.fretux.ascend.player;

import net.fretux.ascend.config.AscendConfig;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    private static final String[] ATTRIBUTE_KEYS = {
            "strength", "agility", "fortitude", "intelligence",
            "willpower", "charisma",
            "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"
    };
    private final Map<String, Integer> attributes = new HashMap<>();
    private int ascendLevel = 1;
    private int ascendXP = 0;
    private int unspentPoints = getPointsPerLevel();
    public PlayerStats() {
        for (String attr : ATTRIBUTE_KEYS) {
            attributes.put(attr, 0);
        }
    }

    public void addAscendXP(int amount) {
        if (amount <= 0 || ascendLevel >= getMaxAscendLevel()) return;
        ascendXP += amount;
        while (ascendLevel < getMaxAscendLevel()) {
            int xpNeeded = getXPToNextAscendLevel();
            if (ascendXP < xpNeeded) break;
            ascendXP -= xpNeeded;
            ascendLevel++;
            unspentPoints += getPointsPerLevel();
            addKnowledgeScaled(1);
        }
        if (ascendLevel >= getMaxAscendLevel()) {
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
        if (ascendLevel >= getMaxAscendLevel()) return 0;
        double base = 100.0;
        double growth = 1.25;
        return (int) Math.ceil(base * Math.pow(growth, ascendLevel - 1) + (ascendLevel * 100));
    }

    public int getUnspentPoints() {
        return unspentPoints;
    }

    public void addUnspentPoints(int amount) {
        unspentPoints = Math.max(0, unspentPoints + amount);
    }

    public boolean spendPoints(String attribute) {
        if (!attributes.containsKey(attribute)) return false;
        int level = attributes.get(attribute);
        if (level >= getMaxAttributePoints()) {
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
        int clamped = Math.max(0, Math.min(level, getMaxAttributePoints()));
        attributes.put(attribute, clamped);
    }

    public int getMaxMana() {
        int intelligence = getAttributeLevel("intelligence");
        int base = 50;        
        int perPoint = 3;    
        return base + intelligence * perPoint;
    }

    private int knowledge = 0;
    private boolean hasUsedMoonseye = false; // Moonseye Tome First Use

    public int getKnowledge() {
        return knowledge;
    }

    public void addKnowledge(int amount) {
        knowledge = Math.max(0, knowledge + amount);
    }

    public int addKnowledgeScaled(int amount) {
        if (amount <= 0) {
            addKnowledge(amount);
            return amount;
        }
        int intelligence = getAttributeLevel("intelligence");
        int scaled = StatEffects.getIntelligenceKnowledgeGain(intelligence, amount);
        addKnowledge(scaled);
        return scaled;
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

    public boolean hasUsedMoonseye() {
        return hasUsedMoonseye;
    }

    public void setHasUsedMoonseye(boolean used) {
        this.hasUsedMoonseye = used;
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
        ascendLevel = Math.max(1, Math.min(ascendLevel, getMaxAscendLevel()));
        ascendXP = tag.contains("AscendXP") ? tag.getInt("AscendXP") : 0;
        if (tag.contains("UnspentPoints")) {
            unspentPoints = Math.max(0, tag.getInt("UnspentPoints"));
        } else {
            unspentPoints = getPointsPerLevel();
        }
        if (tag.contains("Attributes")) {
            CompoundTag attrTag = tag.getCompound("Attributes");
            for (String key : attributes.keySet()) {
                int loaded = attrTag.getInt(key);
                attributes.put(key, Math.max(0, Math.min(loaded, getMaxAttributePoints())));
            }
        }
        if (tag.contains("Knowledge")) {
            knowledge = Math.max(0, tag.getInt("Knowledge"));
        }
        if (tag.contains("HasUsedMoonseye")) {
            hasUsedMoonseye = tag.getBoolean("HasUsedMoonseye");
        }
        int maxXp = getXPToNextAscendLevel();
        if (ascendXP > maxXp) {
            ascendXP = maxXp;
        }
    }

    private static int getPointsPerLevel() {
        return AscendConfig.COMMON.pointsPerLevel.get();
    }

    private static int getMaxAscendLevel() {
        return AscendConfig.COMMON.maxAscendLevel.get();
    }

    private static int getMaxAttributePoints() {
        return AscendConfig.COMMON.maxAttributePoints.get();
    }
}
