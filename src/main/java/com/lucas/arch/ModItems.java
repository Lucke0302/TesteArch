package com.lucas.arch;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

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
        });
    }
}