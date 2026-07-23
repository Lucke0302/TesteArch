package com.lucas.arch.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        // Substituímos o ModConstants pelo ID direto em string
        public static final TagKey<Item> CARNIVORE_FOOD = TagKey.create(
                Registries.ITEM, 
                Identifier.fromNamespaceAndPath("archeology_reimagined", "carnivore_food")
        );
    }
}