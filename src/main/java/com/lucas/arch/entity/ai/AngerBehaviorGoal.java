package com.lucas.arch.entity.ai;

import java.util.EnumSet;
import java.util.List;

import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Trait;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Goal única para o estado de RAIVA (ANGER). Resolve, ao ativar, qual sub-comportamento
 * executar (atacar ou fugir) via BehaviorResolver, e mantém esse sub-comportamento fixo
 * durante toda a execução — evita o ciclo de indecisão.
 *
 * Regras (ver BehaviorResolver.resolveAnger):
 *  - AGGRESSIVENESS, CURIOSITY e GLUTTONY dominantes -> ataca.
 *  - COWARDICE dominante -> foge.
 */
public class AngerBehaviorGoal<T extends TamableAnimal & FeelingDrivenEntity> extends AbstractFuzzyGoal<T> {
    private final double searchRadius = 24.0D;
    private final double attackSpeed = 1.4D;
    private final double fleeSpeed = 1.5D;

    private BehaviorResolver.Behavior activeMode;
    private LivingEntity target;
    private double runX, runY, runZ;

    private int unreachableTicks = 0;
    private int recalcTimer = 0;
    private int attackCooldown = 0;

    private final String attackAnimName;

    public AngerBehaviorGoal(T dino) {
        this(dino, "attack");
    }

    /**
     * @param attackAnimName nome do triggerableAnim registrado no "attack_controller" da
     *                        entidade (ex: "attack" pro Allosaurus, "attack_1" pro Pachy).
     */
    public AngerBehaviorGoal(T dino, String attackAnimName) {
        super(dino, Feeling.ANGER, Trait.AGGRESSIVENESS);
        this.attackAnimName = attackAnimName;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        this.activeMode = BehaviorResolver.resolve(this.dino, Feeling.ANGER);

        AABB box = this.dino.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> entities = this.dino.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this.dino && e.isAlive());

        LivingEntity best = null;
        double closest = Double.MAX_VALUE;
        for (LivingEntity e : entities) {
            if (e.getType() == this.dino.getType()) continue;
            if (e instanceof Player player && (player.isCreative() || player.isSpectator() || this.dino.isOwnedBy(player))) continue;

            double dist = this.dino.distanceToSqr(e);
            if (dist < closest) {
                closest = dist;
                best = e;
            }
        }
        if (best == null) return false;
        this.target = best;

        if (this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK) {
            return true;
        }

        Vec3 fleePos = DefaultRandomPos.getPosAway(this.dino, 16, 7, this.target.position());
        if (fleePos == null) return false;
        if (this.target.distanceToSqr(fleePos.x, fleePos.y, fleePos.z) < this.target.distanceToSqr(this.dino)) {
            return false;
        }

        this.runX = fleePos.x;
        this.runY = fleePos.y;
        this.runZ = fleePos.z;
        return true;
    }

    @Override
    protected boolean canFuzzyContinue() {
        if (this.target == null || !this.target.isAlive()) return false;

        return this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK
                ? this.dino.distanceToSqr(this.target) < searchRadius * searchRadius && this.unreachableTicks < 60
                : !this.dino.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.unreachableTicks = 0;
        this.recalcTimer = 0;
        this.attackCooldown = 0;

        if (this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK) {
            this.dino.getNavigation().moveTo(this.target, attackSpeed);
        } else {
            this.dino.getNavigation().moveTo(this.runX, this.runY, this.runZ, fleeSpeed);
        }
    }

    @Override
    public void tick() {
        if (this.activeMode != BehaviorResolver.Behavior.HUNT_ATTACK) {
            return;
        }

        this.dino.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        double distToTarget = this.dino.distanceToSqr(this.target);
        double attackReach = (this.dino.getBbWidth() * 2.0F * this.dino.getBbWidth() * 2.0F)
                + this.target.getBbWidth() + 1.5F;

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (distToTarget <= attackReach) {
            this.unreachableTicks = 0;

            if (this.attackCooldown <= 0) {
                if (this.dino.level() instanceof ServerLevel serverLevel) {
                    this.dino.doHurtTarget(serverLevel, this.target);
                }
                if (this.dino instanceof com.geckolib.animatable.GeoEntity geoEntity) {
                    geoEntity.triggerAnim("attack_controller", this.attackAnimName);
                }
                this.attackCooldown = 20;
            }
        } else {
            if (this.recalcTimer <= 0) {
                boolean pathStarted = this.dino.getNavigation().moveTo(this.target, attackSpeed);
                this.recalcTimer = 15;
                if (!pathStarted || this.dino.getNavigation().isDone()) {
                    this.unreachableTicks++;
                } else {
                    this.unreachableTicks = 0;
                }
            } else {
                this.recalcTimer--;
                if (this.dino.getNavigation().isDone()) {
                    this.unreachableTicks++;
                }
            }
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.target = null;
        this.dino.getNavigation().stop();
    }
}