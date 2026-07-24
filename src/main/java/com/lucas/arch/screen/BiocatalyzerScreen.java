package com.lucas.arch.screen;

import com.lucas.arch.ArcheologyReimagined;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BiocatalyzerScreen extends AbstractContainerScreen<BiocatalyzerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "textures/gui/fuser.png");
    private static final Identifier PROGRESS_TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "textures/gui/cleansing_bar.png");
    private static final Identifier LAVA_TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, "textures/gui/cleansing_lava.png");

    public BiocatalyzerScreen(BiocatalyzerMenu menu, Inventory playerInventory, Component title) {
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

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, this.imageWidth, this.imageHeight, 240, 164);

        int fuelTime = this.menu.getFuelTime();
        int maxFuelTime = this.menu.getMaxFuelTime();
        if (fuelTime > 0 && maxFuelTime > 0) {
            int alturaAtual = (fuelTime * 85) / maxFuelTime;
            graphics.blit(RenderPipelines.GUI_TEXTURED, LAVA_TEXTURE, x + 13, y + 8 + 85 - alturaAtual, 0f, (float) (85 - alturaAtual), 10, alturaAtual, 10, 85);
        }

        int progress = this.menu.getProcessProgress();
        int maxProgress = this.menu.getMaxProcessTime();
        if (progress > 0 && maxProgress > 0) {
            int steppedProgress = (progress / 20) * 20;
            int currentWidth = (steppedProgress * 17) / maxProgress;
            if (currentWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, PROGRESS_TEXTURE, x + 108, y + 31, 0f, 0f, currentWidth, 11, 17, 11);
            }
        }
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (this.isHovering(13, 8, 10, 85, mouseX, mouseY)) {
            int maxFuelTime = this.menu.getMaxFuelTime();
            int fuelPercent = maxFuelTime > 0 ? (this.menu.getFuelTime() * 100) / maxFuelTime : 0;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§6Combustível: " + fuelPercent + "%"), mouseX, mouseY);
        }
        if (this.isHovering(108, 31, 17, 11, mouseX, mouseY)) {
            int maxProgress = this.menu.getMaxProcessTime();
            int percent = maxProgress > 0 ? (this.menu.getProcessProgress() * 100) / maxProgress : 0;
            graphics.setTooltipForNextFrame(this.font, Component.literal("§aProgresso: " + percent + "%"), mouseX, mouseY);
        }
    }
}