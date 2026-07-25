package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;

public class DinosaurTemptGoal extends TemptGoal {
    private final AllosaurusEntity dino;

    public DinosaurTemptGoal(AllosaurusEntity dino, double speedModifier, Ingredient items, boolean canScare) {
        super(dino, speedModifier, items, canScare);
        this.dino = dino;
    }

    @Override
    public boolean canUse() {
        if (super.canUse()) {
            if (this.dino.isTame() && this.player != null) {
                return this.player == this.dino.getOwner();
            }
            return true; 
        }
        return false;
    }
}