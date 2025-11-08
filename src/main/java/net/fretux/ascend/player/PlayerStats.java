package net.fretux.ascend.player;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    private final Map<String, Integer> attributes = new HashMap<>();
    private final Map<String, Integer> xp = new HashMap<>();
    private int unspentPoints = 15;
    private int ascendLevel = 1;
    private int ascendXP = 0;
    private static final int MAX_ASCEND_LEVEL = 20;

    public PlayerStats() {
        for (String attr : new String[]{
                "strength", "agility", "fortitude", "intelligence",
                "willpower", "charisma",
                "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"}) {
            attributes.put(attr, 0);
            xp.put(attr, 0);
        }
    }
    
    public void addUnspentPoints(int amount) {
        unspentPoints += amount;
    }

    public int getUnspentPoints() {
        return unspentPoints;
    }

    public boolean spendPoints(String attribute) {
        if (!attributes.containsKey(attribute)) return false;
        int cost = getCostToUpgrade(attribute);
        if (unspentPoints < cost) return false;
        unspentPoints -= cost;
        attributes.put(attribute, attributes.get(attribute) + 1);
        return true;
    }

    public int getCostToUpgrade(String attribute) {
        return 1;
    }
    
    public void addXP(String attribute, int amount) {
        if (!xp.containsKey(attribute) || amount <= 0) return;
        int currentXP = xp.get(attribute) + amount;
        int level = attributes.getOrDefault(attribute, 0);
        boolean changed = false;
        while (true) {
            int xpNeeded = getXPToNextAttributeLevel(attribute, level);
            if (xpNeeded <= 0 || currentXP < xpNeeded) break;
            currentXP -= xpNeeded;
            level++;
            changed = true;
            unspentPoints += 1;
            grantAscendXP(10);
        }

        if (changed) {
            attributes.put(attribute, level);
        }
        xp.put(attribute, currentXP);
    }
    
    private int getXPToNextAttributeLevel(String attribute, int level) {
        return 20 + (level * 10);
    }

    public int getXP(String attribute) {
        return xp.getOrDefault(attribute, 0);
    }

    public int getXPToNextLevel(String attribute) {
        int level = attributes.getOrDefault(attribute, 0);
        return getXPToNextAttributeLevel(attribute, level);
    }

    public int getAttributeLevel(String attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    public void setAttributeLevel(String attribute, int level) {
        attributes.put(attribute, level);
    }
    
    public int getAscendLevel() {
        return ascendLevel;
    }

    public int getAscendXP() {
        return ascendXP;
    }
    
    public int getXPToNextAscendLevel() {
        if (ascendLevel >= MAX_ASCEND_LEVEL) return Integer.MAX_VALUE;
        return 50 + (ascendLevel * 25);
    }
    
    private void grantAscendXP(int amount) {
        if (amount <= 0 || ascendLevel >= MAX_ASCEND_LEVEL) return;
        ascendXP += amount;
        while (ascendLevel < MAX_ASCEND_LEVEL) {
            int needed = getXPToNextAscendLevel();
            if (ascendXP < needed) break;
            ascendXP -= needed;
            ascendLevel++;
        }
        if (ascendLevel >= MAX_ASCEND_LEVEL) {
            ascendXP = 0;
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("UnspentPoints", unspentPoints);
        tag.putInt("AscendLevel", ascendLevel);
        tag.putInt("AscendXP", ascendXP);
        CompoundTag attrTag = new CompoundTag();
        CompoundTag xpTag = new CompoundTag();
        for (String key : attributes.keySet()) {
            attrTag.putInt(key, attributes.get(key));
            xpTag.putInt(key, xp.get(key));
        }
        tag.put("Attributes", attrTag);
        tag.put("XP", xpTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("UnspentPoints")) {
            unspentPoints = tag.getInt("UnspentPoints");
        } else {
            unspentPoints = 15;
        }
        
        if (tag.contains("AscendLevel")) {
            ascendLevel = tag.getInt("AscendLevel");
        } else {
            ascendLevel = 1;
        }
        
        if (tag.contains("AscendXP")) {
            ascendXP = tag.getInt("AscendXP");
        } else {
            ascendXP = 0;
        }
        
        if (tag.contains("Attributes")) {
            CompoundTag attrTag = tag.getCompound("Attributes");
            for (String key : attributes.keySet()) {
                attributes.put(key, attrTag.getInt(key));
            }
        }
        
        if (tag.contains("XP")) {
            CompoundTag xpTag = tag.getCompound("XP");
            for (String key : xp.keySet()) {
                xp.put(key, xpTag.getInt(key));
            }
        }
    }
}