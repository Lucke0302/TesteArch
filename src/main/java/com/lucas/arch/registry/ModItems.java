package com.lucas.arch.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

import com.lucas.arch.item.DnaItem;
import com.lucas.arch.item.EncyclopediaItem;

public class ModItems {
    public static final String MOD_ID = "archeologyunnoficial";

    public static final Item UNKNOWN_PLANT_FOSSIL = registerItem("unknown_plant_fossil", Item::new);
    public static final Item UNKNOWN_REPTILE_FOSSIL = registerItem("unknown_reptile_fossil", Item::new);
    public static final Item UNKNOWN_FISH_FOSSIL = registerItem("unknown_fish_fossil", Item::new);
    public static final Item UNKNOWN_MAMMAL_FOSSIL = registerItem("unknown_mammal_fossil", Item::new);
    public static final Item DEFAULT_PLANT_DNA = registerItem("default_plant_dna", 
        properties -> new DnaItem(properties.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0)));
        
    public static final Item DEFAULT_REPTILE_DNA = registerItem("default_reptile_dna", 
        properties -> new DnaItem(properties.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0)));

    public static final Item DEFAULT_MAMMAL_DNA = registerItem("default_mammal_dna", 
        properties -> new DnaItem(properties.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0)));
        
    public static final Item DEFAULT_FISH_DNA = registerItem("default_fish_dna", 
        properties -> new DnaItem(properties.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0)));

    public static final Item ENCYCLOPEDIA = registerItem("encyclopedia", 
        properties -> new EncyclopediaItem(properties.stacksTo(1)));

    public static final Item PROCESSING_BOARD = registerItem("processing_board", Item::new); 
    public static final Item THERMAL_MODULE = registerItem("thermal_module", Item::new); 
    public static final Item CULTURE_CHAMBER = registerItem("culture_chamber", Item::new); 

    public static final Item BASIC_ORGANIC_FUEL = registerItem("basic_organic_fuel", Item::new); 
    public static final Item MEDIUM_ORGANIC_FUEL = registerItem("medium_organic_fuel", Item::new); 
    public static final Item ADVANCED_ORGANIC_FUEL = registerItem("advanced_organic_fuel", Item::new); 
    public static final Item MEAT_CLUSTER = registerItem("meat_cluster", Item::new); 

    public static final Item ALLOSAURUS_EGG = registerItem("allosaurus_egg",
        properties -> new DnaItem(properties.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0))); 

    public static final Item ALLOSAURUS_EMBRYO = registerItem("allosaurus_embryo",
        properties -> new DnaItem(properties.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0)));

    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(
            BuiltInRegistries.ITEM, 
            Identifier.fromNamespaceAndPath(MOD_ID, name),
            function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name))))
        );
    }

    public static void registerModItems() {
        System.out.println("[" + MOD_ID + "] Registrando fósseis customizados...");

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(UNKNOWN_PLANT_FOSSIL);
            output.accept(UNKNOWN_REPTILE_FOSSIL);
            output.accept(UNKNOWN_FISH_FOSSIL);
            output.accept(UNKNOWN_MAMMAL_FOSSIL);
            output.accept(DEFAULT_PLANT_DNA);
            output.accept(DEFAULT_REPTILE_DNA);
            output.accept(ENCYCLOPEDIA);
            output.accept(PROCESSING_BOARD);
            output.accept(THERMAL_MODULE);
            output.accept(CULTURE_CHAMBER);
            output.accept(BASIC_ORGANIC_FUEL);
            output.accept(MEDIUM_ORGANIC_FUEL);
            output.accept(ADVANCED_ORGANIC_FUEL);
            output.accept(MEAT_CLUSTER);
            output.accept(ALLOSAURUS_EGG);
            output.accept(ALLOSAURUS_EMBRYO);
        });
    }
}