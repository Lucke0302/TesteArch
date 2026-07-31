package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Trait;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class FearBehaviorGoal<T extends TamableAnimal & FeelingDrivenEntity> extends AbstractFuzzyGoal<T> {
    private final double searchRadius = 16.0D;
    private final double fleeSpeed = 1.5D;
    private final double attackSpeed = 1.3D;

    private BehaviorResolver.Behavior activeMode;
    private LivingEntity threat;
    private double runX, runY, runZ;
    private int attackCooldown = 0;

    public FearBehaviorGoal(T dino) {
        super(dino, Feeling.FEAR, Trait.COWARDICE);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        this.activeMode = BehaviorResolver.resolve(this.dino, Feeling.FEAR);

        if (this.dino instanceof com.lucas.arch.entity.AbstractFlyingDinosaurEntity flyer
            && flyer.isFlying()
            && this.activeMode != BehaviorResolver.Behavior.HUNT_ATTACK) {
            return false;
        }

        AABB box = this.dino.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> entities = this.dino.level().getEntitiesOfClass(LivingEntity.class, box, e ->
                (e instanceof Player p && !p.isCreative() && !p.isSpectator() && !this.dino.isOwnedBy(p)) ||
                (e.getType() == this.dino.getType() && e != this.dino));
        if (entities.isEmpty()) return false;

        entities.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.threat = entities.get(0);

        if (this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK) {
            return true;
        }

        Vec3 fleePos = DefaultRandomPos.getPosAway(this.dino, 16, 7, this.threat.position());
        if (fleePos == null) return false;
        if (this.threat.distanceToSqr(fleePos.x, fleePos.y, fleePos.z) < this.threat.distanceToSqr(this.dino)) return false;

        this.runX = fleePos.x;
        this.runY = fleePos.y;
        this.runZ = fleePos.z;
        return true;
    }

    @Override
    protected boolean canFuzzyContinue() {
        if (this.threat == null || !this.threat.isAlive()) return false;
        return this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK
                ? this.dino.distanceToSqr(this.threat) < searchRadius * searchRadius
                : !this.dino.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.attackCooldown = 0;
        if (this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK) {
            this.dino.getNavigation().moveTo(this.threat, attackSpeed);
        } else {
            this.dino.getNavigation().moveTo(this.runX, this.runY, this.runZ, fleeSpeed);
        }
    }

    @Override
    public void tick() {
        if (this.activeMode != BehaviorResolver.Behavior.HUNT_ATTACK) return;

        this.dino.getLookControl().setLookAt(this.threat, 30.0F, 30.0F);
        double distSq = this.dino.distanceToSqr(this.threat);
        double reach = (this.dino.getBbWidth() * 2.0F * this.dino.getBbWidth() * 2.0F) + this.threat.getBbWidth() + 1.5F;

        if (this.attackCooldown > 0) this.attackCooldown--;

        if (distSq <= reach) {
            if (this.attackCooldown <= 0 && this.dino.level() instanceof ServerLevel serverLevel) {
                this.dino.doHurtTarget(serverLevel, this.threat);
                this.attackCooldown = 20;
            }
        } else {
            this.dino.getNavigation().moveTo(this.threat, attackSpeed);
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.threat = null;
        this.dino.getNavigation().stop();
    }
}