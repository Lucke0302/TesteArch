package com.lucas.arch;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class CleansingTableScreen extends AbstractContainerScreen<CleansingTableMenu> {
    
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_table.png");

    private static final int WATER_COLOR = 0xFF2266DD;
    private static final int LAVA_COLOR = 0xFFE07A1A;

    public CleansingTableScreen(CleansingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 240, 200);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelY = 10000; 
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            x, y,
            0f, 0f,
            this.imageWidth, this.imageHeight,
            240, 200
        );

        int tanqueLargura = 9; 
        int tanqueAlturaMax = 84;

        int waterLevel = this.menu.getWaterLevel();
        if (waterLevel > 0) {
            int alturaAtual = (waterLevel * tanqueAlturaMax) / 10;
            int aguaX = x + 7; 
            int aguaY = y + 93 - alturaAtual; 

            graphics.fill(aguaX, aguaY, aguaX + tanqueLargura, aguaY + alturaAtual, WATER_COLOR);
        }

        int fuelTime = this.menu.getFuelTime();
        int maxFuelTime = this.menu.getMaxFuelTime();
        if (fuelTime > 0 && maxFuelTime > 0) {
            int alturaAtual = (fuelTime * tanqueAlturaMax) / maxFuelTime;
            int lavaX = x + 21; 
            int lavaY = y + 93 - alturaAtual;

            graphics.fill(lavaX, lavaY, lavaX + tanqueLargura, lavaY + alturaAtual, LAVA_COLOR);
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        
        if (this.isHovering(7, 9, 9, 84, mouseX, mouseY)) {
            int waterPercent = (this.menu.getWaterLevel() * 100) / 10;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§bÁgua: " + waterPercent + "%"), mouseX, mouseY);
        }

        if (this.isHovering(21, 9, 9, 84, mouseX, mouseY)) {
            int maxFuelTime = this.menu.getMaxFuelTime();
            int fuelPercent = maxFuelTime > 0 ? (this.menu.getFuelTime() * 100) / maxFuelTime : 0;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§6Combustível: " + fuelPercent + "%"), mouseX, mouseY);
        }
    }
}