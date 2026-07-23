package com.lucas.arch.entity.ai;

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

    public SeekDroppedFoodGoal(PathfinderMob mob, double speedModifier, double searchRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private double stopDistanceSqr() {
        double stopDist = (this.mob.getBbWidth() / 2.0) + 1.0;
        return stopDist * stopDist;
    }

    @Override
    public boolean canUse() {
        List<ItemEntity> items = this.mob.level().getEntitiesOfClass(
                ItemEntity.class,
                this.mob.getBoundingBox().inflate(this.searchRadius),
                item -> item.getItem().is(ModTags.Items.CARNIVORE_FOOD)
        );
        if (items.isEmpty()) return false;

        items.sort((a, b) -> Double.compare(this.mob.distanceToSqr(a), this.mob.distanceToSqr(b)));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.targetItem.getX(), this.targetItem.getY(), this.targetItem.getZ(), this.speedModifier);
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

        this.mob.getLookControl().setLookAt(this.targetItem);

        if (this.mob.distanceToSqr(this.targetItem) <= stopDistanceSqr()) {
            this.mob.getNavigation().stop();
            this.targetItem.discard();

            if (this.mob instanceof com.geckolib.animatable.GeoEntity geoEntity) {
                geoEntity.triggerAnim("attack_controller", "attack");
            }
            return;
        }

        if (this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().moveTo(this.targetItem.getX(), this.targetItem.getY(), this.targetItem.getZ(), this.speedModifier);
        }
    }
}