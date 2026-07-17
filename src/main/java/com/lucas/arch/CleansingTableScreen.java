package com.lucas.arch;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

public class CleansingTableScreen extends AbstractContainerScreen<CleansingTableMenu> {
    
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "textures/gui/cleansing_table.png");

    public CleansingTableScreen(CleansingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelY = 10000; 
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // --- TOOLTIPS DOS TANQUES (Ajuste as zonas de colisão X, Y, Largura, Altura) ---
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Tooltip da Água (Exemplo: X=40, Y=20)
        if (isHovering(40, 20, 16, 50, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("§bÁgua: " + this.menu.getWaterLevel() + " / 10 Baldes"), mouseX, mouseY);
        }

        // Tooltip do Combustível (Exemplo: X=60, Y=20)
        if (isHovering(60, 20, 16, 50, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("§6Combustível: " + this.menu.getFuelTime() + " Ticks"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 1. Renderiza o seu fundo estático do Pixilart
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // --- CONFIGURAÇÕES DE TAMANHO DOS TANQUES --- //
        int tanqueLargura = 16; 
        int tanqueAlturaMax = 50; // Altura máxima do tanque cheio em pixels

        // 2. RENDERIZAÇÃO DA ÁGUA (Com 70% de Opacidade)
        int waterLevel = this.menu.getWaterLevel();
        if (waterLevel > 0) {
            int alturaAtual = (waterLevel * tanqueAlturaMax) / 10;
            
            // Posição na tela (Ajuste esses valores para encaixar no seu layout)
            int aguaX = x + 40; 
            int aguaY = y + 70 - alturaAtual; // O Y base (70) é o fundo do tanque

            // Puxa a textura oficial de água do Minecraft
            TextureAtlasSprite waterSprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(Identifier.fromNamespaceAndPath("minecraft", "block/water_still"));

            // Ativa a transparência no motor gráfico
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F); // 70% de Alpha (Transparência)
            
            // Desenha a água
            guiGraphics.blit(aguaX, aguaY, 0, tanqueLargura, alturaAtual, waterSprite);
            
            // Reseta a cor e desativa a transparência para não bugar o resto do jogo
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        // 3. RENDERIZAÇÃO DA LAVA/FOGO (Opaco)
        int fuelTime = this.menu.getFuelTime();
        int maxFuelTime = this.menu.getMaxFuelTime();
        if (fuelTime > 0 && maxFuelTime > 0) {
            int alturaAtual = (fuelTime * tanqueAlturaMax) / maxFuelTime;
            
            int lavaX = x + 60; // Do lado da água
            int lavaY = y + 70 - alturaAtual;

            // Puxa a textura oficial de lava
            TextureAtlasSprite lavaSprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(Identifier.fromNamespaceAndPath("minecraft", "block/lava_still"));

            // Desenha a lava direto (sem transparência)
            guiGraphics.blit(lavaX, lavaY, 0, tanqueLargura, alturaAtual, lavaSprite);
        }
    }
}