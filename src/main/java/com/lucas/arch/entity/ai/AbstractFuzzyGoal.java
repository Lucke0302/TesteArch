package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

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
        this.checkCooldown = 100;
        
        if (this.dino.getFeeling(this.feeling) >= 0.75f) {
            return canFuzzyActivate();
        }
        return false;
    }

    protected abstract boolean canFuzzyActivate();

    @Override
    public boolean canContinueToUse() {
        return this.dino.getFeeling(this.feeling) >= 0.65f && canFuzzyContinue();
    }

    protected abstract boolean canFuzzyContinue();

    @Override
    public void stop() {
        float traitVal = this.dino.getTrait(this.associatedTrait);
        float baseDeduction = 0.10f + (this.dino.getRandom().nextFloat() * 0.10f);
        float finalDeduction = baseDeduction - (traitVal / 10.0f);
        
        float currentFeeling = this.dino.getFeeling(this.feeling);
        this.dino.setFeeling(this.feeling, Math.max(0.0f, currentFeeling - finalDeduction));
        
        this.onFuzzyStop();
    }

    protected void onFuzzyStop() {}
}