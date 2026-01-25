package net.fretux.ascend.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fretux.ascend.network.PacketHandler;
import net.fretux.ascend.network.ServerboundShrineChoicePacket;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ShrineScreen extends Screen {

    private static final int WIDTH = 260;
    private static final int HEIGHT = 200;

    private int leftPos;
    private int topPos;

    private int dialogueStage = 0; // 0 = intro, 1 = offer, 2 = confirm
    private int playerKnowledge = 0;
    private final boolean essenceMode;

    public ShrineScreen(boolean essenceMode) {
        super(essenceMode
                ? Component.literal("Remembrance Essence")
                : Component.literal("Shrine of Remembrance"));
        this.essenceMode = essenceMode;
    }
    
    public ShrineScreen() {
        this(false);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                playerKnowledge = stats.getKnowledge();
            });
        }

        updateButtons();
    }

    private void updateButtons() {
        this.clearWidgets();
        int buttonY = topPos + 85;

        // ESSENCE MODE — skip all dialogue, go straight to options
        if (essenceMode) {
            addRenderableWidget(Button.builder(Component.literal("FORGET"), (btn) -> {
                PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("forget"));
                Minecraft.getInstance().setScreen(null);
            }).bounds(leftPos + 20, buttonY, WIDTH - 40, 20).build());

            int y = buttonY + 25;

            addRenderableWidget(Button.builder(Component.literal("UNDERSTAND"), (btn) -> {
                PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("understand"));
                Minecraft.getInstance().setScreen(null);
            }).bounds(leftPos + 20, y, WIDTH - 40, 20).build());
            y += 25;

            addRenderableWidget(Button.builder(Component.literal("REST"), (btn) -> {
                PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("rest"));
                Minecraft.getInstance().setScreen(null);
            }).bounds(leftPos + 20, y, WIDTH - 40, 20).build());
            y += 25;

            addRenderableWidget(Button.builder(Component.literal("LEAVE"), (btn) -> {
                Minecraft.getInstance().setScreen(null);
            }).bounds(leftPos + 20, y, WIDTH - 40, 20).build());

            return;
        }
        switch (dialogueStage) {
            case 0 -> { // Intro
                Button engageButton = Button.builder(Component.literal("I WISH TO ENGAGE IN A DEAL."), (btn) -> {
                    dialogueStage = 1;
                    updateButtons();
                }).bounds(leftPos + 20, buttonY, WIDTH - 40, 20).build();

                Button leaveButton = Button.builder(Component.literal("LEAVE."), (btn) -> {
                    Minecraft.getInstance().setScreen(null);
                }).bounds(leftPos + 20, buttonY + 25, WIDTH - 40, 20).build();

                addRenderableWidget(engageButton);
                addRenderableWidget(leaveButton);
            }

            case 1 -> { // Shrine explains terms
                Button agreeButton = Button.builder(Component.literal("I AGREE."), (btn) -> {
                    dialogueStage = 2;
                    updateButtons();
                }).bounds(leftPos + 20, buttonY, WIDTH - 40, 20).build();

                Button leaveButton = Button.builder(Component.literal("LEAVE."), (btn) -> {
                    Minecraft.getInstance().setScreen(null);
                }).bounds(leftPos + 20, buttonY + 25, WIDTH - 40, 20).build();

                addRenderableWidget(agreeButton);
                addRenderableWidget(leaveButton);
            }

            case 2 -> {
                // Existing Ascend option
                Button forgetButton = Button.builder(Component.literal("I WISH TO FORGET."), (btn) -> {
                    PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("forget"));
                    Minecraft.getInstance().setScreen(null);
                }).bounds(leftPos + 20, buttonY, WIDTH - 40, 20).build();
                addRenderableWidget(forgetButton);
                int y = buttonY + 25;
                if (net.fretux.ascend.compat.AscendMMCompat.isMindMotionPresent()) {
                    Button understandButton = Button.builder(Component.literal("I WISH TO UNDERSTAND."), (btn) -> {
                        PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("understand"));
                        Minecraft.getInstance().setScreen(null);
                    }).bounds(leftPos + 20, y, WIDTH - 40, 20).build();
                    addRenderableWidget(understandButton);
                    y += 25;
                    Button restButton = Button.builder(Component.literal("I WISH TO REST."), (btn) -> {
                        PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("rest"));
                        Minecraft.getInstance().setScreen(null);
                    }).bounds(leftPos + 20, y, WIDTH - 40, 20).build();
                    addRenderableWidget(restButton);
                    y += 25;
                }
                Button leaveButton = Button.builder(Component.literal("LEAVE."), (btn) -> {
                    Minecraft.getInstance().setScreen(null);
                }).bounds(leftPos + 20, y, WIDTH - 40, 20).build();
                addRenderableWidget(leaveButton);
            }
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        RenderSystem.enableBlend();
        gui.fill(leftPos - 8, topPos - 8, leftPos + WIDTH + 8, topPos + HEIGHT + 8, 0xDD0D0B14);
        gui.fill(leftPos - 4, topPos - 4, leftPos + WIDTH + 4, topPos + HEIGHT + 4, 0xE01A1424);
        gui.fill(leftPos - 4, topPos - 4, leftPos + WIDTH + 4, topPos + 20, 0xFF2A1F3A);
        gui.fill(leftPos - 4, topPos + 20, leftPos + WIDTH + 4, topPos + 21, 0x66FFFFFF);
        RenderSystem.disableBlend();

        int titleColor = 0xCDA1FF;
        gui.drawCenteredString(this.font, "Shrine of Remembrance", this.width / 2, topPos + 6, 0xFFFFFF);

        gui.drawCenteredString(this.font, "Knowledge: " + playerKnowledge, this.width / 2, topPos + 28, 0x8FEBD2);

        switch (dialogueStage) {
            case 0 -> {
                gui.drawCenteredString(this.font, "SPEAK, LITTLE ONE.", this.width / 2, topPos + 46, titleColor);
                gui.drawCenteredString(this.font, "WHAT IS IT YOU SEEK TO UNMAKE?", this.width / 2, topPos + 62, titleColor);
            }
            case 1 -> {
                gui.drawCenteredString(this.font, "THE TERMS ARE AS FOLLOWS:", this.width / 2, topPos + 46, titleColor);
                gui.drawCenteredString(this.font, "I WILL CONSUME YOUR KNOWLEDGE.", this.width / 2, topPos + 62, titleColor);
                gui.drawCenteredString(this.font, "YOU WILL REAP A BENEFIT.", this.width / 2, topPos + 78, titleColor);
            }
            case 2 -> {
                gui.drawCenteredString(this.font, "SPEAK YOUR DESIRE, FOOLISH ONE.", this.width / 2, topPos + 52, titleColor);
            }
        }

        super.render(gui, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
