package com.lucas.arch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcheologyUnnoficial implements ModInitializer {
    public static final String MOD_ID = "archeologyunnoficial";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        ModDataComponentTypes.registerDataComponentTypes();
        
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModMenuTypes.registerMenuTypes();
        
        ModItems.registerModItems();
        ModLootTableModifiers.modifyLootTables();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}