package net.fretux.ascend.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fretux.ascend.network.PacketHandler;
import net.fretux.ascend.network.ServerboundSpendPointPacket;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class StatsScreen extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 240;

    private int leftPos;
    private int topPos;

    private final Minecraft mc = Minecraft.getInstance();

    private static final String[] ATTRIBUTES = new String[]{
            "strength", "agility", "fortitude", "intelligence",
            "willpower", "charisma",
            "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"
    };

    private final Map<String, Button> plusButtons = new HashMap<>();

    // --- Tooltips for attributes ---
    private static final Map<String, Component> ATTRIBUTE_TOOLTIPS = new HashMap<>();

    static {
        ATTRIBUTE_TOOLTIPS.put("strength", Component.literal(
                "Strength: Increases melee damage, knockback and armor penetration."
        ));
        ATTRIBUTE_TOOLTIPS.put("agility", Component.literal(
                "Agility: Increases movement speed and mobility."
        ));
        ATTRIBUTE_TOOLTIPS.put("fortitude", Component.literal(
                "Fortitude: Increases max health and knockback resistance."
        ));
        ATTRIBUTE_TOOLTIPS.put("intelligence", Component.literal(
                "Intelligence: Boosts mana, spell power, mana regen & cooldown reduction (with Iron's Spellbooks)."
        ));
        ATTRIBUTE_TOOLTIPS.put("willpower", Component.literal(
                "Willpower: Reduces sanity drain and improves stamina/tempo in harsh conditions."
        ));
        ATTRIBUTE_TOOLTIPS.put("charisma", Component.literal(
                "Charisma: Increases max mana and improves trade deals."
        ));
        ATTRIBUTE_TOOLTIPS.put("light_scaling", Component.literal(
                "Light Scaling: Increases damage with light weapons that scale with this stat."
        ));
        ATTRIBUTE_TOOLTIPS.put("medium_scaling", Component.literal(
                "Medium Scaling: Increases damage with medium weapons that scale with this stat."
        ));
        ATTRIBUTE_TOOLTIPS.put("heavy_scaling", Component.literal(
                "Heavy Scaling: Increases damage with heavy weapons that scale with this stat."
        ));
        ATTRIBUTE_TOOLTIPS.put("magic_scaling", Component.literal(
                "Magic Scaling: Increases spell power, mana regen & cooldown reduction for scaling spells."
        ));
    }

    // which attribute label the mouse is currently over (for tooltip)
    private String hoveredAttributeKey = null;

    public StatsScreen() {
        super(Component.literal("Ascend Stats"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

        this.plusButtons.clear();
        this.clearWidgets();

        int y = topPos + 55;
        for (String attr : ATTRIBUTES) {
            final String key = attr;
            Button plus = Button.builder(Component.literal("+"), (button) -> {
                        PacketHandler.INSTANCE.sendToServer(new ServerboundSpendPointPacket(key));
                    })
                    .bounds(leftPos + WIDTH - 40, y - 6, 30, 18)
                    .build();
            this.addRenderableWidget(plus);
            this.plusButtons.put(key, plus);
            y += 16;
        }

        Button done = Button.builder(Component.translatable("gui.done"), (btn) -> onClose())
                .bounds(leftPos + WIDTH / 2 - 40, topPos + HEIGHT - 24, 80, 20)
                .build();
        this.addRenderableWidget(done);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        RenderSystem.enableBlend();
        gui.fill(leftPos - 4, topPos - 4, leftPos + WIDTH + 4, topPos + HEIGHT + 4, 0xAA000000);
        RenderSystem.disableBlend();
        gui.drawCenteredString(this.font, this.title, this.width / 2, topPos + 8, 0xFFFFFF);
        hoveredAttributeKey = null;
        if (mc.player != null) {
            mc.player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                int level = stats.getAscendLevel();
                int xp = stats.getAscendXP();
                int xpNext = stats.getXPToNextAscendLevel();
                int unspent = stats.getUnspentPoints();
                String levelStr = "Level: " + level + "/" + 20;
                gui.drawString(font, levelStr, leftPos + 10, topPos + 24, 0xFFFF55);
                if (level < 20) {
                    String xpStr = "Ascend XP: " + xp + " / " + xpNext;
                    gui.drawString(font, xpStr, leftPos + 10, topPos + 36, 0xAAAAAA);
                } else {
                    gui.drawString(font, "Ascend XP: MAX", leftPos + 10, topPos + 36, 0xAAAAAA);
                }
                String ptsStr = "Unspent Points: " + unspent;
                int ptsWidth = font.width(ptsStr);
                gui.drawString(font, ptsStr,
                        leftPos + WIDTH - 10 - ptsWidth, topPos + 24, 0x55FF55);
                int y = topPos + 55;
                for (String key : ATTRIBUTES) {
                    int attrLevel = stats.getAttributeLevel(key);
                    int cost = stats.getCostToUpgrade(key);
                    String niceName = formatAttributeName(key);
                    String label = niceName + ": " + attrLevel;
                    int labelX = leftPos + 10;
                    gui.drawString(font, label, labelX, y, 0xFFFFFF);
                    Button plus = plusButtons.get(key);
                    if (plus != null) {
                        plus.setX(leftPos + WIDTH - 40);
                        plus.setY(y - 6);
                        plus.active = (unspent >= cost);
                        plus.setMessage(Component.literal("+" + cost));
                    }
                    int labelWidth = font.width(label);
                    int labelHeight = font.lineHeight;
                    if (mouseX >= labelX && mouseX <= labelX + labelWidth
                            && mouseY >= y && mouseY <= y + labelHeight) {
                        hoveredAttributeKey = key;
                    }
                    y += 16;
                }
            });
        }
        super.render(gui, mouseX, mouseY, partialTicks);
        if (hoveredAttributeKey != null) {
            Component tooltip = ATTRIBUTE_TOOLTIPS.get(hoveredAttributeKey);
            if (tooltip != null) {
                gui.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    private String formatAttributeName(String key) {
        String withSpaces = key.replace('_', ' ');
        return withSpaces.substring(0, 1).toUpperCase() + withSpaces.substring(1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}