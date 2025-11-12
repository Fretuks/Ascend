package net.fretux.ascend.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fretux.ascend.network.ServerStatsPayload;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class StatsScreen extends Screen {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 240;

    private int leftPos;
    private int topPos;

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<String, Button> plusButtons = new HashMap<>();
    private String hoveredAttributeKey = null;

    public StatsScreen() {
        super(Component.literal("Ascend Stats"));
    }

    private static final String[] ATTRIBUTES = new String[]{
            "strength", "agility", "fortitude", "intelligence",
            "willpower", "charisma",
            "light_scaling", "medium_scaling", "heavy_scaling", "magic_scaling"
    };

    // Use translation keys for attribute labels and tooltips
    private static final Map<String, Component> ATTRIBUTE_TOOLTIPS = new HashMap<>();

    static {
        for (String key : ATTRIBUTES) {
            ATTRIBUTE_TOOLTIPS.put(key, Component.translatable("ascend.attribute." + key + ".desc"));
        }
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
                        PacketDistributor.sendToServer(new ServerStatsPayload(key));
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
        this.renderBackground(gui, mouseX, mouseY,  partialTicks);
        RenderSystem.enableBlend();
        gui.fill(leftPos - 4, topPos - 4, leftPos + WIDTH + 4, topPos + HEIGHT + 4, 0xAA000000);
        RenderSystem.disableBlend();
        gui.drawCenteredString(this.font, Component.translatable("ascend.stats.title"), this.width / 2, topPos + 8, 0xFFFFFF);

        hoveredAttributeKey = null;
        if (mc.player != null) {
            var stats = mc.player.getData(PlayerStatsProvider.PLAYER_STATS);
            int level = stats.getAscendLevel();
            int xp = stats.getAscendXP();
            int xpNext = stats.getXPToNextAscendLevel();
            int unspent = stats.getUnspentPoints();

            gui.drawString(font, Component.translatable("ascend.stats.level", level, 20), leftPos + 10, topPos + 24, 0xFFFF55);
            if (level < 20) {
                gui.drawString(font, Component.translatable("ascend.stats.xp", xp, xpNext), leftPos + 10, topPos + 36, 0xAAAAAA);
            } else {
                gui.drawString(font, Component.translatable("ascend.stats.xp_max"), leftPos + 10, topPos + 36, 0xAAAAAA);
            }
            gui.drawString(font, Component.translatable("ascend.stats.unspent", unspent),
                    leftPos + WIDTH - 10 - font.width("Unspent Points: " + unspent), topPos + 24, 0x55FF55);

            int y = topPos + 55;
            for (String key : ATTRIBUTES) {
                int attrLevel = stats.getAttributeLevel(key);
                int cost = stats.getCostToUpgrade(key);
                Component name = Component.translatable("ascend.attribute." + key);
                Component label = Component.translatable("ascend.attribute.display", name, attrLevel);
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
                if (mouseX >= labelX && mouseX <= labelX + labelWidth
                        && mouseY >= y && mouseY <= y + font.lineHeight) {
                    hoveredAttributeKey = key;
                }
                y += 16;
            }
        }

        super.render(gui, mouseX, mouseY, partialTicks);

        if (hoveredAttributeKey != null) {
            Component tooltip = ATTRIBUTE_TOOLTIPS.get(hoveredAttributeKey);
            if (tooltip != null) {
                gui.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}