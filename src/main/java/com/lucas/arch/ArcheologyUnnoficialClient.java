package com.lucas.arch;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class ArcheologyUnnoficialClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.CLEANSING_TABLE_MENU, CleansingTableScreen::new);
    }
}