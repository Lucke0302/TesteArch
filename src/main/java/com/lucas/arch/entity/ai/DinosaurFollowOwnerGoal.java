package com.lucas.arch.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

public class DinosaurFollowOwnerGoal extends Goal {
    private final TamableAnimal dino;
    private LivingEntity owner;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;

    /**
     * @param dino 
     * @param speedModifier 
     * @param startDistance 
     * @param stopDistance 
     */
    public DinosaurFollowOwnerGoal(TamableAnimal dino, double speedModifier, float startDistance, float stopDistance) {
        this.dino = dino;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.navigation = dino.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingOwner = this.dino.getOwner();
        
        if (livingOwner == null) {
            return false;
        } else if (livingOwner.isSpectator()) {
            return false;
        } else if (this.dino.distanceToSqr(livingOwner) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingOwner;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return this.dino.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
    }

    @Override
    public void tick() {
        this.dino.getLookControl().setLookAt(this.owner, 10.0F, (float)this.dino.getMaxHeadXRot());
        
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.navigation.moveTo(this.owner, this.speedModifier);
        }
    }
}