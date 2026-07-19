package com.lucas.arch;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class GuideBookRecipe extends CustomRecipe {
    public static final GuideBookRecipe INSTANCE = new GuideBookRecipe();
    public static final MapCodec<GuideBookRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, GuideBookRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<GuideBookRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private GuideBookRecipe() {
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasBook = false;
        boolean hasFossil = false;
        
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.BOOK) && !hasBook) {
                hasBook = true;
            } else if (isUnknownFossil(stack) && !hasFossil) {
                hasFossil = true;
            } else {
                return false;
            }
        }
        return hasBook && hasFossil;
    }

    private static boolean isUnknownFossil(ItemStack stack) {
        return stack.is(ModItems.UNKNOWN_PLANT_FOSSIL)
            || stack.is(ModItems.UNKNOWN_REPTILE_FOSSIL)
            || stack.is(ModItems.UNKNOWN_MAMMAL_FOSSIL)
            || stack.is(ModItems.UNKNOWN_FISH_FOSSIL);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return ArcheologyUnnoficial.createGuideBook();
    }

    @Override
    public RecipeSerializer<GuideBookRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}