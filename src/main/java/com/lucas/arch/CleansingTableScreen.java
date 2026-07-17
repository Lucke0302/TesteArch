package com.lucas.arch;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class CleansingTableScreen extends AbstractContainerScreen<CleansingTableMenu> {
    
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_table.png");
    
    private static final Identifier WATER_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final Identifier LAVA_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "block/lava_still");

    public CleansingTableScreen(CleansingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 256, 256);
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
            256, 256
        );

        int tanqueLargura = 11; 
        int tanqueAlturaMax = 86;

        int waterLevel = this.menu.getWaterLevel();
        if (waterLevel > 0) {
            int alturaAtual = (waterLevel * tanqueAlturaMax) / 10;
            int aguaX = x + 4; 
            int aguaY = y + 95 - alturaAtual; 

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WATER_TEXTURE, aguaX, aguaY, tanqueLargura, alturaAtual);
        }

        int fuelTime = this.menu.getFuelTime();
        int maxFuelTime = this.menu.getMaxFuelTime();
        if (fuelTime > 0 && maxFuelTime > 0) {
            int alturaAtual = (fuelTime * tanqueAlturaMax) / maxFuelTime;
            int lavaX = x + 18; 
            int lavaY = y + 95 - alturaAtual;

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LAVA_TEXTURE, lavaX, lavaY, tanqueLargura, alturaAtual);
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        
        if (this.isHovering(4, 9, 11, 86, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, Component.literal("§bÁgua: " + this.menu.getWaterLevel() + " / 10 Baldes"), mouseX, mouseY);
        }

        if (this.isHovering(18, 9, 11, 86, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, Component.literal("§6Combustível: " + this.menu.getFuelTime() + " Ticks"), mouseX, mouseY);
        }
    }
}