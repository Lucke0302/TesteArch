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
import com.lucas.arch.item.ArchItem;

public class ModItems {
    public static final String MOD_ID = "archeologyunnoficial";

    // --- Fósseis Originais (F&A) ---
    public static final Item UNKNOWN_PLANT_FOSSIL = registerItem("unknown_plant_fossil", 
        p -> new ArchItem(p, "F&A", "Lucke0302"));
    public static final Item UNKNOWN_REPTILE_FOSSIL = registerItem("unknown_reptile_fossil", 
        p -> new ArchItem(p, "F&A", "Lucke0302"));
    public static final Item UNKNOWN_FISH_FOSSIL = registerItem("unknown_fish_fossil", 
        p -> new ArchItem(p, "F&A", "Lucke0302"));
    public static final Item UNKNOWN_MAMMAL_FOSSIL = registerItem("unknown_mammal_fossil", 
        p -> new ArchItem(p, "F&A", "Lucke0302"));
    public static final Item AMBER = registerItem("amber", 
        p -> new ArchItem(p, "F&A", "Lucke0302"));

    // --- DNAs (Assumindo que você alterou o sprite base) ---
    public static final Item DEFAULT_PLANT_DNA = registerItem("default_plant_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));             
    public static final Item DEFAULT_REPTILE_DNA = registerItem("default_reptile_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));
    public static final Item DEFAULT_MAMMAL_DNA = registerItem("default_mammal_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));             
    public static final Item DEFAULT_FISH_DNA = registerItem("default_fish_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));

    // --- Itens Exclusivos / Novos Designs ---
    public static final Item ENCYCLOPEDIA = registerItem("encyclopedia", 
         p -> new EncyclopediaItem(p.stacksTo(1), "Lucke0302", "Lucke0302"));

    public static final Item PROCESSING_BOARD = registerItem("processing_board", 
        p -> new ArchItem(p, "Lucke0302", "Lucke0302")); 
    public static final Item THERMAL_MODULE = registerItem("thermal_module", 
        p -> new ArchItem(p, "Lucke0302", "Lucke0302")); 
    public static final Item CULTURE_CHAMBER = registerItem("culture_chamber", 
        p -> new ArchItem(p, "Lucke0302", "Lucke0302")); 

    public static final Item BASIC_ORGANIC_FUEL = registerItem("basic_organic_fuel", 
        p -> new ArchItem(p, "EduGuter", "Lucke0302")); 
    public static final Item MEDIUM_ORGANIC_FUEL = registerItem("medium_organic_fuel", 
        p -> new ArchItem(p, "EduGuter", "Lucke0302")); 
    public static final Item ADVANCED_ORGANIC_FUEL = registerItem("advanced_organic_fuel", 
        p -> new ArchItem(p, "EduGuter", "Lucke0302")); 
    public static final Item MEAT_CLUSTER = registerItem("meat_cluster", 
        p -> new ArchItem(p, "EduGuter", "Lucke0302")); 

    // --- Ovos e Embriões ---
    public static final Item ALLOSAURUS_EGG = registerItem("allosaurus_egg",
        p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302")); 
    public static final Item ALLOSAURUS_EMBRYO = registerItem("allosaurus_embryo",
        p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A, Lucke0302", "Lucke0302"));

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
            output.accept(AMBER);
        });
    }
}