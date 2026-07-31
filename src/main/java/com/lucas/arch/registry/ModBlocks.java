package com.lucas.arch.registry;

import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.block.AllosaurusEggBlock;
import com.lucas.arch.block.ArchBrushableBlock;
import com.lucas.arch.block.BiocatalyzerBlock;
import com.lucas.arch.block.BitterBerryBushBlock;
import com.lucas.arch.block.CleansingTableBlock;
import com.lucas.arch.block.CycadCenterBlock;
import com.lucas.arch.block.CycadSaplingBlock;
import com.lucas.arch.block.DimorphodonEggBlock;
import com.lucas.arch.block.FuserBlock;
import com.lucas.arch.block.PachycephalosaurusEggBlock;
import com.lucas.arch.block.ParasaurolophusEggBlock;
import com.lucas.arch.block.QuetzalcoatlusEggBlock;
import com.lucas.arch.block.SpinosaurusEggBlock;
import com.lucas.arch.block.SequoiaSaplingBlock;
import com.lucas.arch.block.SynthesizerBlock;
import com.lucas.arch.item.AllosaurusEggBlockItem;
import com.lucas.arch.item.ArchBlockItem;
import com.lucas.arch.item.DimorphodonEggBlockItem;
import com.lucas.arch.item.PachycephalosaurusEggBlockItem;
import com.lucas.arch.item.ParasaurolophusEggBlockItem;
import com.lucas.arch.item.QuetzalcoatlusEggBlockItem;
import com.lucas.arch.item.SpinosaurusEggBlockItem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;

import java.util.function.Function;

public class ModBlocks {

    public static final Block CLEANSING_TABLE = registerBlock("cleansing_table",
            properties -> new CleansingTableBlock(properties.mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()),
            "F&A", "Lucke0302");

    public static final Block SYNTHESIZER = registerBlock("synthesizer",
            properties -> new SynthesizerBlock(properties.mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()),
            "F&A", "Lucke0302");

    public static final Block FUSER = registerBlock("fuser",
            properties -> new FuserBlock(properties.mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()),
            "F&A, Lucke0302", "Lucke0302");

    public static final Block FOSSIL = registerBlock("fossil",
            properties -> new Block(properties.mapColor(MapColor.STONE).strength(3.0f, 3.0f).requiresCorrectToolForDrops()),
            "F&A", "Lucke0302");

    public static final Block AMBER_ORE = registerBlock("amber_ore",
            properties -> new Block(properties.mapColor(MapColor.STONE).strength(3.0f, 3.0f).requiresCorrectToolForDrops()),
            "F&A", "Lucke0302");

    public static final Block BRUSHED_SAND = registerBlockWithoutItem("brushed_sand", 
            properties -> new ArchBrushableBlock(SoundEvents.BRUSH_SAND, SoundEvents.BRUSH_SAND_COMPLETED, properties.mapColor(MapColor.SAND).strength(0.5f).sound(net.minecraft.world.level.block.SoundType.SUSPICIOUS_SAND)));

    public static final Block BRUSHED_GRAVEL = registerBlockWithoutItem("brushed_gravel", 
            properties -> new ArchBrushableBlock(SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, properties.mapColor(MapColor.STONE).strength(0.6f).sound(net.minecraft.world.level.block.SoundType.SUSPICIOUS_GRAVEL)));

    public static final Block BRUSHED_TUFF = registerBlockWithoutItem("brushed_tuff", 
            properties -> new ArchBrushableBlock(SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, properties.mapColor(MapColor.TERRACOTTA_BROWN).strength(1.5f).requiresCorrectToolForDrops().sound(net.minecraft.world.level.block.SoundType.TUFF)));
        
    private static Block registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> function) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, name));
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, function.apply(BlockBehaviour.Properties.of().setId(blockKey)));
    }

    public static final Block BITTER_BERRY_BUSH = registerBlockWithoutItem("bitter_berry_bush",
            properties -> new BitterBerryBushBlock( 
                properties.mapColor(MapColor.PLANT)
                          .noCollision()
                          .randomTicks()
                          .instabreak()
                          .sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)
            ));

    public static final Block SEQUOIA_SAPLING = registerBlock("sequoia_sapling",
            properties -> new SequoiaSaplingBlock(
                properties.mapColor(MapColor.PLANT)
                        .noCollision()
                        .instabreak()
                        .sound(net.minecraft.world.level.block.SoundType.GRASS)
            ), "Lucke0302", "Lucke0302");

    public static final Block CYCAD_CENTER = registerBlockWithoutItem("cycad_center",
        properties -> new CycadCenterBlock(properties.mapColor(MapColor.WOOD).strength(1.0f)));

    public static final Block CYCAD_SAPLING = registerBlock("cycad_sapling",
        properties -> new CycadSaplingBlock(
            properties.mapColor(MapColor.PLANT)
                      .noCollision()
                      .instabreak()
                      .sound(net.minecraft.world.level.block.SoundType.GRASS)
        ), "Lucke0302", "Lucke0302");

    public static final Block CYCAD_LOG = registerBlock("cycad_log",
        properties -> new Block(properties.mapColor(MapColor.WOOD)
                                        .strength(2.0f)
                                        .sound(net.minecraft.world.level.block.SoundType.WOOD)
                                        .noOcclusion()), 
        "Lucke0302", "Lucke0302");

    public static final Block BIOCATALYZER = registerBlock("biocatalyzer",
        properties -> new BiocatalyzerBlock(properties.mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()),
        "Lucke0302", "Lucke0302");

    public static final Block ALLOSAURUS_EGG_BLOCK = registerBlock("allosaurus_egg_block",
        properties -> new AllosaurusEggBlock(properties.mapColor(MapColor.SAND)
            .noCollision().strength(0.0f).sound(SoundType.BONE_BLOCK).noOcclusion()),
        (block, itemProps) -> new AllosaurusEggBlockItem(block, itemProps, "F&A, Lucke0302", "Lucke0302"));

    public static final Block PACHYCEPHALOSAURUS_EGG_BLOCK = registerBlock("pachycephalosaurus_egg_block",
        properties -> new PachycephalosaurusEggBlock(properties.mapColor(MapColor.SAND)
            .noCollision().strength(0.0f).sound(SoundType.BONE_BLOCK).noOcclusion()),
        (block, itemProps) -> new PachycephalosaurusEggBlockItem(block, itemProps, "F&A, Lucke0302", "Lucke0302"));

    public static final Block SPINOSAURUS_EGG_BLOCK = registerBlock("spinosaurus_egg_block",
        properties -> new SpinosaurusEggBlock(properties.mapColor(MapColor.SAND)
            .noCollision().strength(0.0f).sound(SoundType.BONE_BLOCK).noOcclusion()),
        (block, itemProps) -> new SpinosaurusEggBlockItem(block, itemProps, "F&A, Lucke0302", "Lucke0302"));

    public static final Block PARASAUROLOPHUS_EGG_BLOCK = registerBlock("parasaurolophus_egg_block",
        properties -> new ParasaurolophusEggBlock(properties.mapColor(MapColor.SAND)
            .noCollision().strength(0.0f).sound(SoundType.BONE_BLOCK).noOcclusion()),
        (block, itemProps) -> new ParasaurolophusEggBlockItem(block, itemProps, "F&A, Lucke0302", "Lucke0302"));

    public static final Block QUETZALCOATLUS_EGG_BLOCK = registerBlock("quetzalcoatlus_egg_block",
        properties -> new QuetzalcoatlusEggBlock(properties.mapColor(MapColor.SAND)
            .noCollision().strength(0.0f).sound(SoundType.BONE_BLOCK).noOcclusion()),
        (block, itemProps) -> new QuetzalcoatlusEggBlockItem(block, itemProps, "F&A, Lucke0302", "Lucke0302"));

    public static final Block DIMORPHODON_EGG_BLOCK = registerBlock("dimorphodon_egg_block",
        properties -> new DimorphodonEggBlock(properties.mapColor(MapColor.SAND)
            .noCollision().strength(0.0f).sound(SoundType.BONE_BLOCK).noOcclusion()),
        (block, itemProps) -> new DimorphodonEggBlockItem(block, itemProps, "F&A, Lucke0302", "Lucke0302"));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function, String designer, String programmer) {
        return registerBlock(name, function,
            (block, itemProps) -> new ArchBlockItem(block, itemProps, designer, programmer));
    }

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> blockFunction,
                                        java.util.function.BiFunction<Block, Item.Properties, Item> itemFunction) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, blockFunction.apply(BlockBehaviour.Properties.of().setId(blockKey)));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ArcheologyReimagined.MOD_ID, name));
        Item blockItem = itemFunction.apply(block, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void registerModBlocks() {
        System.out.println("[" + ArcheologyReimagined.MOD_ID + "] Registrando Blocos...");
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(CLEANSING_TABLE);
            output.accept(SYNTHESIZER);
            output.accept(FUSER);
            output.accept(FOSSIL);
            output.accept(AMBER_ORE);
            output.accept(CYCAD_LOG);
            output.accept(BIOCATALYZER);
            output.accept(ALLOSAURUS_EGG_BLOCK);
            output.accept(PACHYCEPHALOSAURUS_EGG_BLOCK);
            output.accept(SPINOSAURUS_EGG_BLOCK);
            output.accept(PARASAUROLOPHUS_EGG_BLOCK);
            output.accept(QUETZALCOATLUS_EGG_BLOCK);
            output.accept(DIMORPHODON_EGG_BLOCK);
        });
    }
}