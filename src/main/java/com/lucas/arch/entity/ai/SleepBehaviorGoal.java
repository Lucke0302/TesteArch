package com.lucas.arch.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.TamableAnimal;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.AbstractDinosaurEntity;
import java.util.EnumSet;

public class SleepBehaviorGoal<T extends TamableAnimal & FeelingDrivenEntity> extends Goal {
    private final T dinosaur;

    public SleepBehaviorGoal(T dinosaur) {
        this.dinosaur = dinosaur;
        // As Flags travam a locomoção, cabeça e pulos enquanto dorme
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.dinosaur instanceof AbstractDinosaurEntity dino) {
            return dino.isSleeping();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.dinosaur instanceof AbstractDinosaurEntity dino) {
            return dino.isSleeping();
        }
        return false;
    }

    @Override
    public void start() {
        this.dinosaur.getNavigation().stop();
        this.dinosaur.setTarget(null);
    }
}