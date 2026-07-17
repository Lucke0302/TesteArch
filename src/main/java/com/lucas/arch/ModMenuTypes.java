package com.lucas.arch;

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

    public static void registerMenuTypes() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Menu Types...");
    }
}