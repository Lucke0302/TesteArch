package com.lucas.arch;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipeSerializers {

    public static final RecipeSerializer<GuideBookRecipe> GUIDE_BOOK_RECIPE =
        Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(ArcheologyUnnoficial.MOD_ID, "guide_book_crafting"),
            GuideBookRecipe.SERIALIZER
        );

    public static void registerRecipeSerializers() {
        System.out.println("[" + ArcheologyUnnoficial.MOD_ID + "] Registrando Recipe Serializers...");
    }
}