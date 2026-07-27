package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.HerbivoreDiet;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Fome do herbívoro: NUNCA caça (ver BehaviorResolver — se HUNGER resolver pra HUNT_ATTACK
 * via trait AGGRESSIVENESS dominante, essa goal simplesmente ignora e cai direto pra
 * comida no chão / pastagem). Ordem de prioridade de aquisição:
 *  1. Item do chão tageado HERBIVORE_FOOD (mais nutritivo, mais rápido).
 *  2. grass_block próximo -> vira dirt, concede grazeSaturation().
 */
public class HerbivoreHungerGoal<T extends TamableAnimal & FeelingDrivenEntity & HerbivoreDiet> extends AbstractFuzzyGoal<T> {

    private static final double SEARCH_RADIUS = 16.0D;
    private static final double GROUND_FOOD_SPEED = 1.2D;
    private static final double GRAZE_SPEED = 1.0D;
    private static final float GRAZE_NUTRITION = 3.0f;
    private static final int GRAZE_DURATION_TICKS = 40;

    private enum Mode { ITEM, GRAZE }

    private Mode activeMode;
    private ItemEntity groundFoodTarget;
    private BlockPos grazeTarget;

    private int recalcTimer = 0;
    private int eatCooldown = 0;
    private int grazeProgress = 0;

    public HerbivoreHungerGoal(T dino) {
        super(dino, Feeling.HUNGER, Trait.GLUTTONY);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        // Resolve só pra manter o dominant-state coerente; não usamos o resultado pra decidir
        // caça, porque herbívoro não caça.
        BehaviorResolver.resolve(this.dino, Feeling.HUNGER);

        if (acquireGroundFood()) {
            this.activeMode = Mode.ITEM;
            return true;
        }
        if (acquireGrazeTarget()) {
            this.activeMode = Mode.GRAZE;
            return true;
        }
        return false;
    }

    private boolean acquireGroundFood() {
        List<ItemEntity> items = this.dino.level().getEntitiesOfClass(
                ItemEntity.class,
                this.dino.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> item.getItem().is(ModTags.Items.HERBIVORE_FOOD)
        );
        if (items.isEmpty()) return false;

        items.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.groundFoodTarget = items.get(0);
        return true;
    }

    private boolean acquireGrazeTarget() {
        BlockPos origin = this.dino.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        int radius = 8;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (!this.dino.level().getBlockState(pos).is(Blocks.GRASS_BLOCK)) continue;

                    double dist = origin.distSqr(pos);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos;
                    }
                }
            }
        }

        if (best == null) return false;
        this.grazeTarget = best;
        return true;
    }

    @Override
    protected boolean canFuzzyContinue() {
        return switch (this.activeMode) {
            case ITEM -> {
                if (this.groundFoodTarget == null || !this.groundFoodTarget.isAlive()) {
                    yield acquireGroundFood();
                }
                yield true;
            }
            case GRAZE -> {
                if (this.grazeTarget == null || !this.dino.level().getBlockState(this.grazeTarget).is(Blocks.GRASS_BLOCK)) {
                    yield acquireGrazeTarget();
                }
                yield true;
            }
        };
    }

    @Override
    public void start() {
        this.recalcTimer = 0;
        this.eatCooldown = 0;
        this.grazeProgress = 0;

        if (this.activeMode == Mode.ITEM && this.groundFoodTarget != null) {
            this.dino.getNavigation().moveTo(this.groundFoodTarget, GROUND_FOOD_SPEED);
        } else if (this.grazeTarget != null) {
            this.dino.getNavigation().moveTo(this.grazeTarget.getX() + 0.5, this.grazeTarget.getY(), this.grazeTarget.getZ() + 0.5, GRAZE_SPEED);
        }
    }

    @Override
    public void tick() {
        if (this.eatCooldown > 0) this.eatCooldown--;

        if (this.activeMode == Mode.ITEM && this.groundFoodTarget != null) {
            tickItemFood();
        } else if (this.grazeTarget != null) {
            tickGraze();
        }
    }

    private void tickItemFood() {
        this.dino.getLookControl().setLookAt(this.groundFoodTarget, 30.0F, 30.0F);

        if (this.dino.getBoundingBox().inflate(1.5D).intersects(this.groundFoodTarget.getBoundingBox())) {
            this.dino.getNavigation().stop();

            if (this.eatCooldown <= 0) {
                ItemStack foodStack = this.groundFoodTarget.getItem();

                this.dino.feedSaturation(foodStack);

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
            this.dino.getNavigation().moveTo(this.groundFoodTarget, GROUND_FOOD_SPEED);
            this.recalcTimer = 15;
        } else {
            this.recalcTimer--;
        }
    }

    private void tickGraze() {
        Vec3 targetCenter = Vec3.atCenterOf(this.grazeTarget);
        this.dino.getLookControl().setLookAt(targetCenter.x, targetCenter.y, targetCenter.z, 30.0F, 30.0F);

        double distSq = this.dino.position().distanceToSqr(targetCenter);
        if (distSq <= 4.0D) {
            this.dino.getNavigation().stop();
            this.grazeProgress++;

            if (this.grazeProgress == 1 && this.dino instanceof com.geckolib.animatable.GeoEntity geo) {
                geo.triggerAnim("eat_controller", "eat");
            }

            if (this.grazeProgress >= GRAZE_DURATION_TICKS) {
                if (this.dino.level().getBlockState(this.grazeTarget).is(Blocks.GRASS_BLOCK)) {
                    this.dino.level().setBlock(this.grazeTarget, Blocks.DIRT.defaultBlockState(), 3);
                    this.dino.grazeSaturation(GRAZE_NUTRITION);
                }
                this.grazeTarget = null;
                this.grazeProgress = 0;
            }
            return;
        }

        if (this.recalcTimer <= 0) {
            this.dino.getNavigation().moveTo(targetCenter.x, targetCenter.y, targetCenter.z, GRAZE_SPEED);
            this.recalcTimer = 15;
        } else {
            this.recalcTimer--;
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.groundFoodTarget = null;
        this.grazeTarget = null;
        this.grazeProgress = 0;
        this.dino.getNavigation().stop();
    }
}