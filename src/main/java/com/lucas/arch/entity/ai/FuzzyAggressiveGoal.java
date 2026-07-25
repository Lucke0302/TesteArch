package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FuzzyAggressiveGoal extends AbstractFuzzyGoal {
    private final double searchRadius = 24.0D; 
    private final double speedModifier = 1.4D; 
    private LivingEntity target;

    public FuzzyAggressiveGoal(AllosaurusEntity dino) {
        super(dino, Feeling.ANGER, Trait.AGGRESSIVENESS);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        AABB searchBox = this.dino.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> entities = this.dino.level().getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != this.dino && e.isAlive());
        
        LivingEntity bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (entity.getType() == this.dino.getType()) continue;
            
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) continue;

            double dist = this.dino.distanceToSqr(entity);
            if (dist < closestDist) {
                closestDist = dist;
                bestTarget = entity;
            }
        }

        if (bestTarget != null) {
            this.target = bestTarget;
            return true;
        }
        return false;
    }

    @Override
    protected boolean canFuzzyContinue() {
        return this.target != null && this.target.isAlive() && this.dino.distanceToSqr(this.target) < (searchRadius * searchRadius);
    }

    @Override
    public void start() {
        this.dino.getNavigation().moveTo(this.target, this.speedModifier);
    }

    @Override
    public void tick() {
        this.dino.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        
        double distToTarget = this.dino.distanceToSqr(this.target);
        double attackReach = (this.dino.getBbWidth() * 2.0F * this.dino.getBbWidth() * 2.0F) + this.target.getBbWidth();

        if (distToTarget <= attackReach) {
            if (this.dino.level() instanceof ServerLevel serverLevel) {
                this.dino.doHurtTarget(serverLevel, this.target);
            }
            if (this.dino instanceof com.geckolib.animatable.GeoEntity geoEntity) {
                geoEntity.triggerAnim("attack_controller", "attack");
            }
        } else {
            this.dino.getNavigation().moveTo(this.target, this.speedModifier);
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.target = null;
        this.dino.getNavigation().stop();
    }
}