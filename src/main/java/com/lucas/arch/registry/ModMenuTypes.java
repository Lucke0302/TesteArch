package com.lucas.arch.registry; 

import com.lucas.arch.ArcheologyUnnoficial;
import com.lucas.arch.screen.CleansingTableMenu;
import com.lucas.arch.screen.FuserMenu;
import com.lucas.arch.screen.SynthesizerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final MenuType<CleansingTableMenu> CLEANSING_TABLE_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "cleansing_table_menu"),
                    new MenuType<>(CleansingTableMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static final MenuType<SynthesizerMenu> SYNTHESIZER_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "synthesizer_menu"),
                    new MenuType<>(SynthesizerMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );


    public static final MenuType<FuserMenu> FUSER_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "fuser_menu"),
                    new MenuType<>(FuserMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static void registerMenuTypes() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Menu Types...");
    }
}