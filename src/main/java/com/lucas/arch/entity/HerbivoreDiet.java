package com.lucas.arch.entity;

import net.minecraft.world.item.ItemStack;

/**
 * Contrato para entidades herbívoras.
 * Permite que a HerbivoreHungerGoal alimente a entidade e aplique os ganhos de saturação.
 */
public interface HerbivoreDiet {
    void feedSaturation(ItemStack foodStack);
    void grazeSaturation(float baseNutrition);
}