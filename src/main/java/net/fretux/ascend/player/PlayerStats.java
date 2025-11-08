package net.fretux.ascend.player;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    public static final int MAX_ASCEND_LEVEL = 20;
    public static final int POINTS_PER_LEVEL = 15;
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
        if (unspentPoints <= 0) return false;
        if (!attributes.containsKey(attribute)) return false;
        attributes.put(attribute, attributes.get(attribute) + 1);
        unspentPoints--;
        return true;
    }

    public int getCostToUpgrade(String attribute) {
        return 1;
    }

    public int getAttributeLevel(String attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    public void setAttributeLevel(String attribute, int level) {
        attributes.put(attribute, level);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("AscendLevel", ascendLevel);
        tag.putInt("AscendXP", ascendXP);
        tag.putInt("UnspentPoints", unspentPoints);
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
                if (attrTag.contains(key)) {
                    attributes.put(key, attrTag.getInt(key));
                }
            }
        }
    }
}