package com.lucas.arch.registry;

import com.lucas.arch.ArcheologyUnnoficial;
import com.lucas.arch.block.entity.CleansingTableBlockEntity;
import com.lucas.arch.block.entity.FuserBlockEntity;
import com.lucas.arch.block.entity.SynthesizerBlockEntity;
import com.lucas.arch.block.entity.ArchBrushableBlockEntity;

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

    public static final BlockEntityType<SynthesizerBlockEntity> SYNTHESIZER_BE =
        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "synthesizer_be"),
                FabricBlockEntityTypeBuilder.create(SynthesizerBlockEntity::new, ModBlocks.SYNTHESIZER).build()
        );

    public static final BlockEntityType<ArchBrushableBlockEntity> ARCH_BRUSHABLE_BE = 
        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "arch_brushable_be"),
                FabricBlockEntityTypeBuilder.create(ArchBrushableBlockEntity::new, 
                ModBlocks.BRUSHED_SAND, ModBlocks.BRUSHED_GRAVEL, ModBlocks.BRUSHED_TUFF).build()
        );
            
    public static final BlockEntityType<FuserBlockEntity> FUSER_BE =
    Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "fuser_be"),
        FabricBlockEntityTypeBuilder.create(FuserBlockEntity::new, ModBlocks.FUSER).build()
    );    

    public static void registerBlockEntities() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Block Entities...");
    }
}