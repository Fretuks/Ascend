package net.fretux.ascend.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fretux.ascend.network.PacketHandler;
import net.fretux.ascend.network.ServerboundSpendPointPacket;
import net.fretux.ascend.player.PlayerStats;
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
    private static final int HEIGHT = 210;

    private int leftPos;
    private int topPos;

    private final Minecraft mc = Minecraft.getInstance();

    private static final String[] ATTRIBUTES = new String[]{
            "strength", "agility", "fortitude", "intelligence",
            "willpower", "charisma",
            "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"
    };

    // Track plus buttons so we can update enabled state & labels based on current stats
    private final Map<String, Button> plusButtons = new HashMap<>();

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

        // Create + buttons for each attribute
        int y = topPos + 55;
        for (String attr : ATTRIBUTES) {
            final String key = attr;

            Button plus = Button.builder(Component.literal("+"), (button) -> {
                        // Send spend request to server for this attribute
                        PacketHandler.INSTANCE.sendToServer(new ServerboundSpendPointPacket(key));
                    })
                    .bounds(leftPos + WIDTH - 40, y - 6, 30, 18)
                    .build();

            this.addRenderableWidget(plus);
            this.plusButtons.put(key, plus);

            y += 16;
        }

        // Done/close button
        Button done = Button.builder(Component.translatable("gui.done"), (btn) -> onClose())
                .bounds(leftPos + WIDTH / 2 - 40, topPos + HEIGHT - 24, 80, 20)
                .build();
        this.addRenderableWidget(done);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);

        // Panel background (simple rectangle; swap to texture if you have one)
        RenderSystem.enableBlend();
        gui.fill(leftPos - 4, topPos - 4, leftPos + WIDTH + 4, topPos + HEIGHT + 4, 0xAA000000);
        RenderSystem.disableBlend();

        // Title
        gui.drawCenteredString(this.font, this.title, this.width / 2, topPos + 8, 0xFFFFFF);

        if (mc.player != null) {
            mc.player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                // ==== Top Info: Level, XP, Points ====
                int level = stats.getLevel();
                int xp = stats.getAscendXP();
                int xpNext = stats.getXPForNextLevel(); // 0 if max
                int unspent = stats.getUnspentPoints();

                // Level
                String levelStr = "Level: " + level + "/" + 20;
                gui.drawString(font, levelStr, leftPos + 10, topPos + 24, 0xFFFF55);

                // Ascend XP
                if (level < 20) {
                    String xpStr = "Ascend XP: " + xp + " / " + xpNext;
                    gui.drawString(font, xpStr, leftPos + 10, topPos + 36, 0xAAAAAA);
                } else {
                    gui.drawString(font, "Ascend XP: MAX", leftPos + 10, topPos + 36, 0xAAAAAA);
                }

                // Unspent points (right aligned)
                String ptsStr = "Unspent Points: " + unspent;
                int ptsWidth = font.width(ptsStr);
                gui.drawString(font, ptsStr,
                        leftPos + WIDTH - 10 - ptsWidth, topPos + 24, 0x55FF55);

                // ==== Attributes List ====
                int y = topPos + 55;
                for (String key : ATTRIBUTES) {
                    int attrLevel = stats.getAttributeLevel(key);
                    int sourceXP = stats.getXP(key); // per-activity XP, just for feedback
                    int cost = stats.getCostToUpgrade(key);

                    String niceName = formatAttributeName(key);

                    // Attribute name + level
                    gui.drawString(font,
                            niceName + ": " + attrLevel,
                            leftPos + 10, y,
                            0xFFFFFF);

                    // XP (source-based)
                    gui.drawString(font,
                            "XP: " + sourceXP,
                            leftPos + 110, y,
                            0x777777);

                    // Update + button (position, text, enabled)
                    Button plus = plusButtons.get(key);
                    if (plus != null) {
                        plus.setX(leftPos + WIDTH - 40);
                        plus.setY(y - 6);

                        // Always visible, but only active if enough points
                        plus.active = (unspent >= cost);

                        // Show cost inside button
                        // Keep it short; if too long for your font, switch to just "+"
                        plus.setMessage(Component.literal("+" + cost));
                    }

                    y += 16;
                }
            });
        }

        super.render(gui, mouseX, mouseY, partialTicks);
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