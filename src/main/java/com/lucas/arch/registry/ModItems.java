package com.lucas.arch.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.function.Function;

import com.lucas.arch.item.DnaItem;
import com.lucas.arch.item.EncyclopediaItem;
import com.lucas.arch.item.ArchItem;
import com.lucas.arch.item.ArchItemNameBlockItem;

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
        p -> new ArchItem(p, "F&A, Lucke0302", "Lucke0302"));
    public static final Item MOSQUITO_IN_AMBER = registerItem("mosquito_in_amber",
         p -> new ArchItem(p, "F&A", "Lucke0302"));

    // --- Pós de Escavação ---
    public static final Item SAND_POWDER = registerItem("sand_powder", 
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));
    
    public static final Item GRAVEL_POWDER = registerItem("gravel_powder", 
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));
    
    public static final Item TUFF_POWDER = registerItem("tuff_powder", 
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    // --- DNAs ---
    public static final Item DEFAULT_PLANT_DNA = registerItem("default_plant_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));             
    public static final Item DEFAULT_REPTILE_DNA = registerItem("default_reptile_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));
    public static final Item DEFAULT_MAMMAL_DNA = registerItem("default_mammal_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));             
    public static final Item DEFAULT_FISH_DNA = registerItem("default_fish_dna", 
         p -> new DnaItem(p.stacksTo(1).component(ModDataComponentTypes.DNA_QUALITY, 0), "F&A", "Lucke0302"));   
    public static final Item FRAGMENTED_DNA = registerItem("fragmented_dna",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

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

    // --- Melhoria genética ---
    public static final Item EMPTY_SYRINGE = registerItem("empty_syringe",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    public static final Item FULL_SYRINGE = registerItem("full_syringe",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    public static final Item BIO_PROPELLANT = registerItem("bio_propellant",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    // --- Contenção ---
    public static final Item EMPTY_DART = registerItem("empty_dart",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    public static final Item FULL_DART = registerItem("full_dart",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    // --- Novas plantas ---
    public static final Item BITTER_BERRIES = registerItem("bitter_berries",
         p -> new ArchItemNameBlockItem(ModBlocks.BITTER_BERRY_BUSH, p
              .useItemDescriptionPrefix()
              .food(
                  new net.minecraft.world.food.FoodProperties.Builder()
                      .nutrition(2)
                      .saturationModifier(0.1f)
                      .build(),
                  Consumables.defaultFood()
                      .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), 1.0F))
                      .build()
              ), "Lucke0302", "Lucke0302"));
    
    public static final Item CYCAD_SEED = registerItem("cycad_seed",
         p -> new ArchItem(p, "Lucke0302", "Lucke0302"));

    public static final Item CYCAD_FRUIT = registerItem("cycad_fruit",
         p -> new ArchItem(p.food(
             new FoodProperties.Builder()
                 .nutrition(3)
                 .saturationModifier(0.2f)
                 .build(),
             Consumables.defaultFood()
                 .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 160, 0), 1.0F)) 
                 .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0), 0.8F)) 
                 .build()
         ), "Lucke0302", "Lucke0302"));

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
            output.accept(MOSQUITO_IN_AMBER);
            output.accept(EMPTY_SYRINGE);
            output.accept(FULL_SYRINGE);
            output.accept(EMPTY_DART);
            output.accept(FULL_DART);
            output.accept(FRAGMENTED_DNA);
            output.accept(BITTER_BERRIES);
            output.accept(BIO_PROPELLANT);
            output.accept(SAND_POWDER);
            output.accept(GRAVEL_POWDER);
            output.accept(TUFF_POWDER);
            output.accept(ModBlocks.SEQUOIA_SAPLING);
            output.accept(CYCAD_SEED);
            output.accept(CYCAD_FRUIT);
            output.accept(ModBlocks.CYCAD_SAPLING);
        });
    }
}