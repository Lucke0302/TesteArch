package com.lucas.arch;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final String MOD_ID = "archeologyunnoficial";

    public static final Item UNKNOWN_FOSSIL = registerItem("unknown_fossil", new Item(new Item.Properties()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM, 
            ResourceLocation.fromNamespaceAndPath(MOD_ID, name), 
            item
        );
    }

    public static void registerModItems() {
        System.out.println("[" + MOD_ID + "] Registrando itens do mod...");

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(UNKNOWN_FOSSIL);
        });
    }
}