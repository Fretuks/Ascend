package net.fretux.ascend.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fretux.ascend.network.PacketHandler;
import net.fretux.ascend.network.ServerboundShrineChoicePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ShrineScreen extends Screen {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 120;

    private int leftPos;
    private int topPos;

    public ShrineScreen() {
        super(Component.literal("Shrine of Remembrance"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

        this.clearWidgets();

        // Dialogue text
        int buttonY = topPos + 60;

        Button forgetButton = Button.builder(Component.literal("I WISH TO FORGET"), (btn) -> {
            PacketHandler.INSTANCE.sendToServer(new ServerboundShrineChoicePacket("forget"));
            Minecraft.getInstance().setScreen(null);
        }).bounds(leftPos + 20, buttonY, WIDTH - 40, 20).build();

        Button cancelButton = Button.builder(Component.literal("LEAVE"), (btn) -> {
            Minecraft.getInstance().setScreen(null);
        }).bounds(leftPos + 20, buttonY + 25, WIDTH - 40, 20).build();

        addRenderableWidget(forgetButton);
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        RenderSystem.enableBlend();
        gui.fill(leftPos - 6, topPos - 6, leftPos + WIDTH + 6, topPos + HEIGHT + 6, 0xAA000000);
        RenderSystem.disableBlend();

        gui.drawCenteredString(this.font, "SPEAK, LITTLE ONE.", this.width / 2, topPos + 10, 0xAA55FF);
        gui.drawCenteredString(this.font, "WHAT IS IT YOU SEEK TO UNMAKE?", this.width / 2, topPos + 25, 0xAA55FF);

        super.render(gui, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
