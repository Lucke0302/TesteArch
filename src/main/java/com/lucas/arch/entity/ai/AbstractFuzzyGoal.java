package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class AbstractFuzzyGoal extends Goal {
    protected final AllosaurusEntity dino;
    protected final Feeling feeling;
    protected final Trait associatedTrait;

    private int checkCooldown = 0;

    public AbstractFuzzyGoal(AllosaurusEntity dino, Feeling feeling, Trait trait) {
        this.dino = dino;
        this.feeling = feeling;
        this.associatedTrait = trait;
    }

    @Override
    public boolean canUse() {
        if (this.checkCooldown > 0) {
            this.checkCooldown--;
            return false;
        }
        this.checkCooldown = 20 + this.dino.getRandom().nextInt(20);

        float activationThreshold = this.feeling == Feeling.HUNGER ? 0.3f : 0.75f;

        if (this.dino.getFeeling(this.feeling) >= activationThreshold) {
            if (this.dino.getDominantState() == this.feeling.ordinal() + 1) {
                return canFuzzyActivate();
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        float continueThreshold = this.feeling == Feeling.HUNGER ? 0.15f : 0.65f;

        return this.dino.getFeeling(this.feeling) >= continueThreshold
              && this.dino.getDominantState() == this.feeling.ordinal() + 1
              && canFuzzyContinue();
    }

    @Override
    public void stop() {
        if (this.feeling != Feeling.HUNGER) {
            float traitVal = this.dino.getTrait(this.associatedTrait);
            float baseDeduction = 0.10f + (this.dino.getRandom().nextFloat() * 0.10f);
            float finalDeduction = baseDeduction - (traitVal / 10.0f);
            float currentFeeling = this.dino.getFeeling(this.feeling);
            this.dino.setFeeling(this.feeling, Math.max(0.0f, currentFeeling - finalDeduction));
        }
        this.onFuzzyStop();
    }

    protected void onFuzzyStop() {}

    protected abstract boolean canFuzzyActivate();
    protected abstract boolean canFuzzyContinue();
}