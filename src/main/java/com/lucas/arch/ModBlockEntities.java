package com.lucas.arch;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<CleansingTableBlockEntity> CLEANSING_TABLE_BE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "cleansing_table_be"),
                    FabricBlockEntityTypeBuilder.create(CleansingTableBlockEntity::new, ModBlocks.CLEANSING_TABLE).build()
            );

    public static void registerBlockEntities() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Block Entities...");
    }
}