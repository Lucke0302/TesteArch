package com.lucas.arch;

import com.lucas.arch.registry.ModMenuTypes;
import com.lucas.arch.screen.CleansingTableScreen;
import com.lucas.arch.screen.FuserScreen;
import com.lucas.arch.screen.SynthesizerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import com.lucas.arch.client.renderer.AllosaurusRenderer;
import com.lucas.arch.registry.ModEntities;

public class ArcheologyUnnoficialClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.CLEANSING_TABLE_MENU, CleansingTableScreen::new);

        MenuScreens.register(ModMenuTypes.SYNTHESIZER_MENU, SynthesizerScreen::new);

        MenuScreens.register(ModMenuTypes.FUSER_MENU, FuserScreen::new);

        EntityRenderers.register(ModEntities.ALLOSAURUS, AllosaurusRenderer::new);

    }
}