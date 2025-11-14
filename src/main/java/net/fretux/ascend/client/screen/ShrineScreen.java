package net.fretux.ascend.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fretux.ascend.network.ServerShrinePayload;
import net.fretux.ascend.player.PlayerStats;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class ShrineScreen extends Screen {

    private static final int WIDTH = 260;
    private static final int HEIGHT = 160;

    private int leftPos;
    private int topPos;

    private int dialogueStage = 0; // 0 = intro, 1 = offer, 2 = confirm
    private int playerKnowledge = 0;

    public ShrineScreen() {
        super(Component.literal("Shrine of Remembrance"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            var stats =  mc.player.getData(PlayerStatsProvider.PLAYER_STATS);
                playerKnowledge = stats.getKnowledge();
        }

        updateButtons();
    }

    private void updateButtons() {
        this.clearWidgets();
        int buttonY = topPos + 85;

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

            case 2 -> { // Player picks what to unmake
                Button forgetButton = Button.builder(Component.literal("I WISH TO FORGET."), (btn) -> {
                   PacketDistributor.sendToServer(new ServerShrinePayload("forget"));
                    Minecraft.getInstance().setScreen(null);
                }).bounds(leftPos + 20, buttonY, WIDTH - 40, 20).build();

                Button leaveButton = Button.builder(Component.literal("LEAVE."), (btn) -> {
                    Minecraft.getInstance().setScreen(null);
                }).bounds(leftPos + 20, buttonY + 25, WIDTH - 40, 20).build();

                addRenderableWidget(forgetButton);
                addRenderableWidget(leaveButton);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        gui.fill(0, 0, this.width, this.height, 0xAA000000);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui, mouseX, mouseY,partialTicks);
        RenderSystem.enableBlend();
        gui.fill(leftPos - 6, topPos - 6, leftPos + WIDTH + 6, topPos + HEIGHT + 6, 0xCC000000);
        RenderSystem.disableBlend();

        int titleColor = 0xAA55FF;
        gui.drawCenteredString(this.font, "Shrine of Remembrance", this.width / 2, topPos, 0xFFFFFF);

        gui.drawCenteredString(this.font, "Knowledge: " + playerKnowledge, this.width / 2, topPos + 20, 0x55FFAA);

        switch (dialogueStage) {
            case 0 -> {
                gui.drawCenteredString(this.font, "SPEAK, LITTLE ONE.", this.width / 2, topPos + 40, titleColor);
                gui.drawCenteredString(this.font, "WHAT IS IT YOU SEEK TO UNMAKE?", this.width / 2, topPos + 55, titleColor);
            }
            case 1 -> {
                gui.drawCenteredString(this.font, "THE TERMS ARE AS FOLLOWS:", this.width / 2, topPos + 40, titleColor);
                gui.drawCenteredString(this.font, "I WILL CONSUME YOUR KNOWLEDGE.", this.width / 2, topPos + 55, titleColor);
                gui.drawCenteredString(this.font, "YOU WILL REAP A BENEFIT.", this.width / 2, topPos + 70, titleColor);
            }
            case 2 -> {
                gui.drawCenteredString(this.font, "SPEAK YOUR DESIRE, FOOLISH ONE.", this.width / 2, topPos + 40, titleColor);
            }
        }

        super.render(gui, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}