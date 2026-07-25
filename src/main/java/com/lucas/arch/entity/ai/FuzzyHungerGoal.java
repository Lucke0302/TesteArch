package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FuzzyHungerGoal extends AbstractFuzzyGoal {
    private final double searchRadius = 16.0D;
    private final double speedModifier = 1.3D;
    private LivingEntity target;
    
    private int unreachableTicks = 0;
    private int recalculatePathTimer = 0;
    private int attackCooldown = 0; 
    private boolean isBegging = false;

    public FuzzyHungerGoal(AllosaurusEntity dino) {
        super(dino, Feeling.HUNGER, Trait.GLUTTONY);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        AABB searchBox = this.dino.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> entities = this.dino.level().getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != this.dino && e.isAlive());
        
        float anger = this.dino.getFeeling(Feeling.ANGER);
        float aggro = this.dino.getTrait(Trait.AGGRESSIVENESS);
        
        this.isBegging = (this.dino.getTrait(Trait.CURIOSITY) > aggro) || this.dino.isTame();
        
        LivingEntity bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            boolean isValidTarget = false;
            
            if (entity instanceof net.minecraft.world.entity.animal.cow.Cow || 
                entity instanceof net.minecraft.world.entity.animal.pig.Pig || 
                entity instanceof net.minecraft.world.entity.animal.sheep.Sheep || 
                entity instanceof net.minecraft.world.entity.animal.chicken.Chicken) {
                isValidTarget = true;
            }
            else if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                if (this.isBegging || (anger > 0.7f && aggro > 0.7f)) {
                    isValidTarget = true; 
                }
            }

            if (isValidTarget) {
                double dist = this.dino.distanceToSqr(entity);
                if (entity instanceof Animal) {
                    dist -= 4000.0D; 
                }
                
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = entity;
                }
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
        return this.target != null && this.target.isAlive() 
            && this.dino.distanceToSqr(this.target) < (searchRadius * searchRadius)
            && this.unreachableTicks < 60;
    }

    @Override
    public void start() {
        this.dino.getNavigation().moveTo(this.target, this.speedModifier);
        this.unreachableTicks = 0;
        this.recalculatePathTimer = 0;
        this.attackCooldown = 0;
    }

    @Override
    public void tick() {
        this.dino.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        double distToTarget = this.dino.distanceToSqr(this.target);
        double attackReach = (this.dino.getBbWidth() * 2.0F * this.dino.getBbWidth() * 2.0F) + this.target.getBbWidth() + 1.5F;
        
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (distToTarget <= attackReach) {
            this.unreachableTicks = 0;
            
            if (this.isBegging && this.target instanceof Player) {
                if (this.dino instanceof com.geckolib.animatable.GeoEntity geoEntity && this.dino.tickCount % 40 == 0) { 
                    geoEntity.triggerAnim("main_controller", "speak");
                }
                return;
            }

            if (this.attackCooldown <= 0) {
                if (this.dino.level() instanceof ServerLevel serverLevel) {
                    this.dino.doHurtTarget(serverLevel, this.target);
                }
                if (this.dino instanceof com.geckolib.animatable.GeoEntity geoEntity) {
                    geoEntity.triggerAnim("attack_controller", "attack");
                }
                this.attackCooldown = 20; 
            }
        } else {
            if (this.recalculatePathTimer <= 0) {
                boolean pathStarted = this.dino.getNavigation().moveTo(this.target, this.speedModifier);
                this.recalculatePathTimer = 15;
                if (!pathStarted || this.dino.getNavigation().isDone()) this.unreachableTicks++;
                else this.unreachableTicks = 0;
            } else {
                this.recalculatePathTimer--;
                if (this.dino.getNavigation().isDone()) this.unreachableTicks++;
            }
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.target = null;
        this.dino.getNavigation().stop();
    }
}