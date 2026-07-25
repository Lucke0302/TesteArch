package com.lucas.arch.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> CARNIVORE_FOOD = TagKey.create(
                Registries.ITEM, 
                Identifier.fromNamespaceAndPath("archeology_reimagined", "carnivore_food")
        );
    }

    public static class Blocks {
        public static final TagKey<Block> EGG_HEAT_SOURCES = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath("archeology_reimagined", "egg_heat_sources")
        );
}
}