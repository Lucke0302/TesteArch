package com.lucas.arch.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Contrato adicional para dinossauros carnívoros, usado pela CarnivoreHungerGoal.
 * Implementado junto com FeelingDrivenEntity (ver AllosaurusEntity/SpinosaurusEntity).
 */
public interface CarnivoreDiet {

    /** @param isHuntBonus true quando o alimento veio de uma presa abatida (dobra o valor + cura). */
    void feedSaturation(ItemStack foodStack, boolean isHuntBonus);

    /** Define quais LivingEntity contam como presa válida pra essa espécie. */
    boolean isValidPrey(LivingEntity entity);
}