package com.lucas.arch.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Contrato único para entidades onívoras (ex: pterossauros, dinossauros com dieta mista).
 * Unifica os contratos de CarnivoreDiet e HerbivoreDiet num só lugar,
 * eliminando conflito de assinatura entre feedSaturation(ItemStack, boolean)
 * e feedSaturation(ItemStack) que existiria se uma entidade implementasse ambas.
 *
 * A goal OmnivoreHungerGoal usa esta interface para:
 *  - Caçar presas via isValidPrey()
 *  - Comer itens do chão (carnes, vegetais, peixes, ovos) via feedSaturation()
 *  - Pastar grass_block opcionalmente via grazeSaturation() (default no-op)
 */
public interface OmnivoreDiet {

    /**
     * Alimenta a entidade com um ItemStack do chão ou de presa abatida.
     * @param foodStack o item consumido
     * @param isHuntBonus true se veio de uma presa caçada (dobra valor + cura)
     */
    void feedSaturation(ItemStack foodStack, boolean isHuntBonus);

    /** Define quais LivingEntity contam como presa válida para esta espécie. */
    boolean isValidPrey(LivingEntity entity);

    /**
     * (Opcional) Pastagem de grass_block. Onívoros que não pastam (ex: pterossauros)
     * simplesmente não sobrescrevem — o default é no-op e a goal nunca entra em modo GRAZE.
     */
    default void grazeSaturation(float baseNutrition) {}
}