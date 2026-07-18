package com.lucas.arch;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dicionário de fósseis aceitos pela Cleansing Table e o que cada um pode virar.
 *
 * Chance de sucesso: 30% (SUCCESS_CHANCE). Os outros 70% caem no pool de falha (junk).
 */
public class ModCleansingRecipes {

    public static final float SUCCESS_CHANCE = 0.30F;

    // Todo fóssil que falha pode gerar areia ou cascalho.
    private static final List<CleansingRecipe.WeightedItem> AREIA_E_CASCALHO = List.of(
        new CleansingRecipe.WeightedItem(Items.SAND, 1),
        new CleansingRecipe.WeightedItem(Items.GRAVEL, 1)
    );

    private static List<CleansingRecipe.WeightedItem> junkPool(CleansingRecipe.WeightedItem... extras) {
        List<CleansingRecipe.WeightedItem> pool = new ArrayList<>(AREIA_E_CASCALHO);
        pool.addAll(List.of(extras));
        return pool;
    }

    public static final Map<Item, CleansingRecipe> RECIPES = Map.of(

        ModItems.UNKNOWN_PLANT_FOSSIL, new CleansingRecipe(
            ModItems.UNKNOWN_PLANT_FOSSIL,
            ModItems.DEFAULT_PLANT_DNA,
            junkPool(new CleansingRecipe.WeightedItem(Items.CHARCOAL, 1))
        ),

        ModItems.UNKNOWN_REPTILE_FOSSIL, new CleansingRecipe(
            ModItems.UNKNOWN_REPTILE_FOSSIL,
            ModItems.DEFAULT_REPTILE_DNA,
            junkPool(
                new CleansingRecipe.WeightedItem(Items.BONE_MEAL, 1),
                new CleansingRecipe.WeightedItem(Items.BONE, 1)
            )
        ),

        ModItems.UNKNOWN_MAMMAL_FOSSIL, new CleansingRecipe(
            ModItems.UNKNOWN_MAMMAL_FOSSIL,
            ModItems.DEFAULT_MAMMAL_DNA,
            junkPool(
                new CleansingRecipe.WeightedItem(Items.BONE_MEAL, 1),
                new CleansingRecipe.WeightedItem(Items.BONE, 1)
            )
        ),

        ModItems.UNKNOWN_FISH_FOSSIL, new CleansingRecipe(
            ModItems.UNKNOWN_FISH_FOSSIL,
            ModItems.DEFAULT_FISH_DNA,
            junkPool(new CleansingRecipe.WeightedItem(Items.BONE_MEAL, 1))
        )
    );

    public static boolean isValidInput(Item item) {
        return RECIPES.containsKey(item);
    }

    public static CleansingRecipe get(Item item) {
        return RECIPES.get(item);
    }

    /** Sorteia um item de falha dentro do pool ponderado da receita. */
    public static Item rollFailureItem(CleansingRecipe recipe, net.minecraft.util.RandomSource random) {
        List<CleansingRecipe.WeightedItem> pool = recipe.failureOutputs();
        int totalWeight = pool.stream().mapToInt(CleansingRecipe.WeightedItem::weight).sum();
        int roll = random.nextInt(totalWeight);
        int acc = 0;
        for (CleansingRecipe.WeightedItem entry : pool) {
            acc += entry.weight();
            if (roll < acc) {
                return entry.item();
            }
        }
        return pool.get(pool.size() - 1).item();
    }
}