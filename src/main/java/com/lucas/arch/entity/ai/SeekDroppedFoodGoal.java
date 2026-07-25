package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.EnumSet;
import java.util.List;

public class SeekDroppedFoodGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final double searchRadius;
    private ItemEntity targetItem;
    
    private int unreachableTicks = 0;
    private int recalculatePathTimer = 0;

    public SeekDroppedFoodGoal(PathfinderMob mob, double speedModifier, double searchRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob instanceof AllosaurusEntity allo) {
            float aggro = allo.getTrait(Trait.AGGRESSIVENESS);
            float glut = allo.getTrait(Trait.GLUTTONY);
            float coward = allo.getTrait(Trait.COWARDICE);
            float curio = allo.getTrait(Trait.CURIOSITY);
            
            if (aggro > coward && aggro > curio && glut > coward && glut > curio) {
                if (allo.getFeeling(Feeling.HUNGER) < 0.30f) {
                    return false; 
                }
            }
        }

        List<ItemEntity> items = this.mob.level().getEntitiesOfClass(
                ItemEntity.class,
                this.mob.getBoundingBox().inflate(this.searchRadius),
                item -> item.getItem().is(ModTags.Items.CARNIVORE_FOOD)
        );

        if (items.isEmpty()) {
            return false;
        }

        items.sort((a, b) -> Double.compare(this.mob.distanceToSqr(a), this.mob.distanceToSqr(b)));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive() && this.unreachableTicks < 60;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
        this.unreachableTicks = 0;
        this.recalculatePathTimer = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) {
            return;
        }

        this.mob.getLookControl().setLookAt(this.targetItem, 30.0F, 30.0F);

        if (this.mob.getBoundingBox().inflate(1.5D).intersects(this.targetItem.getBoundingBox())) {
            this.mob.getNavigation().stop();
            if (this.mob instanceof AllosaurusEntity allo) {
                allo.feedSaturation(this.targetItem.getItem(), false);
            }
            this.targetItem.discard();
            if (this.mob instanceof com.geckolib.animatable.GeoEntity geoEntity) {
                geoEntity.triggerAnim("eat_controller", "eat");
            }
            return;
        }

        if (this.recalculatePathTimer <= 0) {
            boolean pathStarted = this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
            this.recalculatePathTimer = 15;
            
            if (!pathStarted || this.mob.getNavigation().isDone()) {
                this.unreachableTicks++;
            } else {
                this.unreachableTicks = 0;
            }
        } else {
            this.recalculatePathTimer--;
            if (this.mob.getNavigation().isDone()) {
                this.unreachableTicks++;
            }
        }
    }
}