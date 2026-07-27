package com.lucas.arch.entity;

/**
 * Contrato mínimo que uma entidade precisa expor para participar do sistema de
 * Feelings/Traits/BehaviorResolver e das goals fuzzy genéricas (AbstractFuzzyGoal,
 * FearBehaviorGoal, AngerBehaviorGoal, CuriosityBehaviorGoal, BehaviorResolver).
 *
 * Toda entidade "senciente" do mod (AllosaurusEntity, PachycephalosaurusEntity, futuras)
 * deve implementar esta interface além de estender TamableAnimal.
 */
public interface FeelingDrivenEntity {
    float getTrait(Trait trait);
    float getFeeling(Feeling feeling);
    void setFeeling(Feeling feeling, float value);
    byte getDominantState();
}