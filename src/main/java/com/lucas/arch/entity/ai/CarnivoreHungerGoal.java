package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.CarnivoreDiet;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Generalizada a partir de AllosaurusHungerGoal. Serve qualquer carnívoro
 * (T deve implementar TamableAnimal + FeelingDrivenEntity + CarnivoreDiet).
 * Quem varia por espécie é a definição de presa válida (T#isValidPrey) e o
 * bônus de saturação por caça (T#feedSaturation), delegados à instância.
 */
public class CarnivoreHungerGoal<T extends TamableAnimal & FeelingDrivenEntity & CarnivoreDiet> extends AbstractFuzzyGoal<T> {

    private final double searchRadius = 16.0D;
    private final double huntSpeed = 1.3D;
    private final double groundFoodSpeed = 1.2D;

    private BehaviorResolver.Behavior activeMode;
    private LivingEntity huntTarget;
    private ItemEntity groundFoodTarget;

    private int unreachableTicks = 0;
    private int recalcTimer = 0;
    private int attackCooldown = 0;
    private int eatCooldown = 0;

    private final String attackAnimName;

    public CarnivoreHungerGoal(T dino) {
        this(dino, "attack");
    }

    public CarnivoreHungerGoal(T dino, String attackAnimName) {
        super(dino, Feeling.HUNGER, Trait.GLUTTONY);
        this.attackAnimName = attackAnimName;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        this.activeMode = BehaviorResolver.resolve(this.dino, Feeling.HUNGER);
        return switch (this.activeMode) {
            case HUNT_ATTACK -> {
                if (acquireHuntTarget()) yield true;
                if (acquireGroundFood()) {
                    this.activeMode = BehaviorResolver.Behavior.SEEK_GROUND_FOOD;
                    yield true;
                }
                yield false;
            }
            case SEEK_GROUND_FOOD -> {
                if (acquireGroundFood()) yield true;
                if (acquireHuntTarget()) {
                    this.activeMode = BehaviorResolver.Behavior.HUNT_ATTACK;
                    yield true;
                }
                yield false;
            }
            case BEG_OWNER -> acquireGlutton();
            default -> false;
        };
    }

    private boolean acquireGlutton() {
        if (acquireGroundFood()) {
            this.activeMode = BehaviorResolver.Behavior.SEEK_GROUND_FOOD;
            return true;
        }
        if (acquireHuntTarget()) {
            this.activeMode = BehaviorResolver.Behavior.HUNT_ATTACK;
            return true;
        }
        return false;
    }

    private boolean acquireGroundFood() {
        List<ItemEntity> items = this.dino.level().getEntitiesOfClass(
                ItemEntity.class,
                this.dino.getBoundingBox().inflate(searchRadius),
                item -> item.getItem().is(ModTags.Items.CARNIVORE_FOOD)
        );
        if (items.isEmpty()) return false;

        items.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.groundFoodTarget = items.get(0);
        return true;
    }

    private boolean acquireHuntTarget() {
        AABB box = this.dino.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> entities = this.dino.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this.dino && e.isAlive() && this.dino.isValidPrey(e));

        LivingEntity best = null;
        double closest = Double.MAX_VALUE;

        for (LivingEntity e : entities) {
            double dist = this.dino.distanceToSqr(e);
            if (e instanceof Animal) dist -= 4000.0D;

            if (dist < closest) {
                closest = dist;
                best = e;
            }
        }

        if (best == null) return false;
        this.huntTarget = best;
        return true;
    }

    @Override
    protected boolean canFuzzyContinue() {
        return switch (this.activeMode) {
            case HUNT_ATTACK -> {
                if (this.huntTarget == null || !this.huntTarget.isAlive() || this.dino.distanceToSqr(this.huntTarget) > searchRadius * searchRadius) {
                    yield acquireHuntTarget();
                }
                yield this.unreachableTicks < 60;
            }
            case SEEK_GROUND_FOOD -> {
                if (this.groundFoodTarget == null || !this.groundFoodTarget.isAlive()) {
                    yield acquireGroundFood();
                }
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public void start() {
        this.unreachableTicks = 0;
        this.recalcTimer = 0;
        this.attackCooldown = 0;
        this.eatCooldown = 0;

        if (this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK && this.huntTarget != null) {
            this.dino.getNavigation().moveTo(this.huntTarget, huntSpeed);
        } else if (this.groundFoodTarget != null) {
            this.dino.getNavigation().moveTo(this.groundFoodTarget, groundFoodSpeed);
        }
    }

    @Override
    public void tick() {
        if (this.eatCooldown > 0) this.eatCooldown--;

        if (this.activeMode == BehaviorResolver.Behavior.HUNT_ATTACK && this.huntTarget != null) {
            tickHunt();
        } else if (this.groundFoodTarget != null) {
            tickGroundFood();
        }
    }

    private void tickHunt() {
        this.dino.getLookControl().setLookAt(this.huntTarget, 30.0F, 30.0F);

        double distSq = this.dino.distanceToSqr(this.huntTarget);
        double reach = (this.dino.getBbWidth() * 2.0F * this.dino.getBbWidth() * 2.0F) + this.huntTarget.getBbWidth() + 1.5F;

        if (this.attackCooldown > 0) this.attackCooldown--;

        if (distSq <= reach) {
            this.unreachableTicks = 0;
            if (this.attackCooldown <= 0 && this.dino.level() instanceof ServerLevel serverLevel) {
                this.dino.doHurtTarget(serverLevel, this.huntTarget);
                if (this.dino instanceof com.geckolib.animatable.GeoEntity geo) {
                    geo.triggerAnim("attack_controller", this.attackAnimName);
                }
                this.attackCooldown = 20;
            }
            return;
        }

        if (this.recalcTimer <= 0) {
            boolean started = this.dino.getNavigation().moveTo(this.huntTarget, huntSpeed);
            this.recalcTimer = 15;
            this.unreachableTicks = (!started || this.dino.getNavigation().isDone()) ? this.unreachableTicks + 1 : 0;
        } else {
            this.recalcTimer--;
            if (this.dino.getNavigation().isDone()) this.unreachableTicks++;
        }
    }

    private void tickGroundFood() {
        this.dino.getLookControl().setLookAt(this.groundFoodTarget, 30.0F, 30.0F);

        if (this.dino.getBoundingBox().inflate(1.5D).intersects(this.groundFoodTarget.getBoundingBox())) {
            this.dino.getNavigation().stop();

            if (this.eatCooldown <= 0) {
                ItemStack foodStack = this.groundFoodTarget.getItem();

                this.dino.feedSaturation(foodStack, false);

                if (this.dino instanceof com.geckolib.animatable.GeoEntity geo) {
                    geo.triggerAnim("eat_controller", "eat");
                }

                foodStack.shrink(1);

                if (foodStack.isEmpty()) {
                    this.groundFoodTarget.discard();
                    this.groundFoodTarget = null;
                } else {
                    this.groundFoodTarget.setItem(foodStack);
                }

                this.eatCooldown = 15;
            }
            return;
        }

        if (this.recalcTimer <= 0) {
            this.dino.getNavigation().moveTo(this.groundFoodTarget, groundFoodSpeed);
            this.recalcTimer = 15;
        } else {
            this.recalcTimer--;
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.huntTarget = null;
        this.groundFoodTarget = null;
        this.dino.getNavigation().stop();
    }
}