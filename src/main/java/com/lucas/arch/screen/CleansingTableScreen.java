package com.lucas.arch.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import com.lucas.arch.ArcheologyUnnoficial;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class CleansingTableScreen extends AbstractContainerScreen<CleansingTableMenu> {
    
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_table.png");
    private static final Identifier PROGRESS_TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_bar.png");
    private static final Identifier WATER_TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_water.png");
    private static final Identifier LAVA_TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_lava.png");

    public CleansingTableScreen(CleansingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 240, 164);
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
            240, 164
        );

        int tanqueLargura = 10; 
        int tanqueAlturaMax = 85; 
        
        int waterLevel = this.menu.getWaterLevel();
        if (waterLevel > 0) {
            int alturaAtual = (waterLevel * tanqueAlturaMax) / 10;

            int aguaX = x + 6;  
            int aguaY = y + 93 - alturaAtual; 

            int corteV = tanqueAlturaMax - alturaAtual;

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                WATER_TEXTURE,
                aguaX, aguaY,
                0f, (float) corteV,   
                tanqueLargura, alturaAtual, 
                10, 85 
            );
        }

        int fuelTime = this.menu.getFuelTime();
        int maxFuelTime = this.menu.getMaxFuelTime();
        if (fuelTime > 0 && maxFuelTime > 0) {
            int alturaAtual = (fuelTime * tanqueAlturaMax) / maxFuelTime;

            int lavaX = x + 20; 
            int lavaY = y + 93 - alturaAtual;

            int corteV = tanqueAlturaMax - alturaAtual;

            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                LAVA_TEXTURE,
                lavaX, lavaY,
                0f, (float) corteV,
                tanqueLargura, alturaAtual,
                10, 85
            );
        }

        int progress = this.menu.getProcessProgress();
        int maxProgress = this.menu.getMaxProcessTime();

        if (progress > 0) {
            int steppedProgress = (progress / 20) * 20;

            int currentWidth = (steppedProgress * 17) / maxProgress;

            if (currentWidth > 0) {
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED, 
                    PROGRESS_TEXTURE,  
                    x + 108, y + 31,       
                    0f, 0f,                 
                    currentWidth, 11,  
                    17, 11 
                );
            }
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        
        // Tooltip do Tanque de Água
        if (this.isHovering(7, 9, 9, 84, mouseX, mouseY)) {
            int waterPercent = (this.menu.getWaterLevel() * 100) / 10;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§bÁgua: " + waterPercent + "%"), mouseX, mouseY);
        }

        // Tooltip do Tanque de Combustível
        if (this.isHovering(21, 9, 9, 84, mouseX, mouseY)) {
            int maxFuelTime = this.menu.getMaxFuelTime();
            int fuelPercent = maxFuelTime > 0 ? (this.menu.getFuelTime() * 100) / maxFuelTime : 0;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§6Combustível: " + fuelPercent + "%"), mouseX, mouseY);
        }

        // Tooltip do Progresso de Limpeza
        if (this.isHovering(108, 31, 17, 11, mouseX, mouseY)) {
            int maxProgress = this.menu.getMaxProcessTime();
            int percent = maxProgress > 0 ? (this.menu.getProcessProgress() * 100) / maxProgress : 0;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§aProgresso: " + percent + "%"), mouseX, mouseY);
        }
    }
}