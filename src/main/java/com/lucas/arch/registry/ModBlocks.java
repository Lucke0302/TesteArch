package com.lucas.arch.registry;

import com.lucas.arch.ArcheologyUnnoficial;
import com.lucas.arch.block.CleansingTableBlock;
import com.lucas.arch.block.FuserBlock;
import com.lucas.arch.block.SynthesizerBlock;
import com.lucas.arch.item.ArchBlockItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

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

    private static Block registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> function) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, name));
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, function.apply(BlockBehaviour.Properties.of().setId(blockKey)));
    }

    public static final Block BITTER_BERRY_BUSH = registerBlockWithoutItem("bitter_berry_bush",
            properties -> new net.minecraft.world.level.block.SweetBerryBushBlock(
                properties.mapColor(MapColor.PLANT)
                          .noCollision()
                          .randomTicks()
                          .instabreak()
                          .sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)
            ));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function, String designer, String programmer) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, function.apply(BlockBehaviour.Properties.of().setId(blockKey)));
        
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, name));
        
        Item blockItem = new ArchBlockItem(block, new Item.Properties().setId(itemKey), designer, programmer);
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        
        return block;
    }

    public static void registerModBlocks() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Blocos...");
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(CLEANSING_TABLE);
            output.accept(SYNTHESIZER);
            output.accept(FUSER);
            output.accept(FOSSIL);
            output.accept(AMBER_ORE);
        });
    }
}