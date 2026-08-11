package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AbstractFlyingDinosaurEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.OmnivoreDiet;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal de fome unificada para onívoros. Substitui a necessidade de registrar
 * CarnivoreHungerGoal + HerbivoreHungerGoal simultaneamente (o que conflitaria
 * por ambos serem AbstractFuzzyGoal com as mesmas flags MOVE|LOOK).
 *
 * Ordem de prioridade de aquisição de alimento:
 *  1. Caça (HUNT_ATTACK) — se trait AGGRESSIVENESS é dominante OU se não há
 *     comida no chão disponível, tenta caçar via isValidPrey().
 *  2. Item do chão (SEEK_GROUND_FOOD) — aceita tanto CARNIVORE_FOOD quanto
 *     HERBIVORE_FOOD. Sempre preferido para traits não-agressivas.
 *  3. Pastagem (GRAZE) — opcional, apenas se a entidade sobrescreveu
 *     grazeSaturation() (default: no-op, então a goal ignora este modo).
 *
 * @param <T> entidade que seja TamableAnimal + FeelingDrivenEntity + OmnivoreDiet
 */
public class OmnivoreHungerGoal<T extends TamableAnimal & FeelingDrivenEntity & OmnivoreDiet> extends AbstractFuzzyGoal<T> {

    private static final double SEARCH_RADIUS = 16.0D;
    private static final double HUNT_SPEED = 1.3D;
    private static final double GROUND_FOOD_SPEED = 1.2D;
    private static final double GRAZE_SPEED = 1.0D;
    private static final float GRAZE_NUTRITION = 3.0f;
    private static final int GRAZE_DURATION_TICKS = 40;

    private enum Mode { HUNT_ATTACK, SEEK_GROUND_FOOD, GRAZE }

    private BehaviorResolver.Behavior activeBehavior;
    private Mode activeMode;
    private LivingEntity huntTarget;
    private ItemEntity groundFoodTarget;
    private BlockPos grazeTarget;

    private int unreachableTicks = 0;
    private int recalcTimer = 0;
    private int attackCooldown = 0;
    private int eatCooldown = 0;
    private int grazeProgress = 0;

    private final String attackAnimName;
    private final boolean canGraze;

    public OmnivoreHungerGoal(T dino) {
        this(dino, "attack");
    }

    public OmnivoreHungerGoal(T dino, String attackAnimName) {
        super(dino, Feeling.HUNGER, Trait.GLUTTONY);
        this.attackAnimName = attackAnimName;
        this.canGraze = hasOverriddenGraze();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Verifica via reflection se a entidade sobrescreveu o default no-op de grazeSaturation.
     * Se não sobrescreveu, a goal nem tenta pastar — evita busca desnecessária de grass_block.
     */
    private boolean hasOverriddenGraze() {
        try {
            return !this.dino.getClass().getMethod("grazeSaturation", float.class).isDefault();
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    protected boolean canFuzzyActivate() {
        this.activeBehavior = BehaviorResolver.resolve(this.dino, Feeling.HUNGER);

        if (this.activeBehavior == BehaviorResolver.Behavior.BEG_OWNER) {
            if (acquireGroundFood()) {
                this.activeMode = Mode.SEEK_GROUND_FOOD;
                return true;
            }
            if (acquireHuntTarget()) {
                this.activeMode = Mode.HUNT_ATTACK;
                return true;
            }
            if (this.canGraze && acquireGrazeTarget()) {
                this.activeMode = Mode.GRAZE;
                return true;
            }
            return false;
        }

        if (this.activeBehavior == BehaviorResolver.Behavior.HUNT_ATTACK) {
            if (acquireHuntTarget()) {
                this.activeMode = Mode.HUNT_ATTACK;
                return true;
            }
            if (acquireGroundFood()) {
                this.activeMode = Mode.SEEK_GROUND_FOOD;
                return true;
            }
            if (this.canGraze && acquireGrazeTarget()) {
                this.activeMode = Mode.GRAZE;
                return true;
            }
            return false;
        }

        if (acquireGroundFood()) {
            this.activeMode = Mode.SEEK_GROUND_FOOD;
            return true;
        }

        if (acquireHuntTarget()) {
            this.activeMode = Mode.HUNT_ATTACK;
            return true;
        }
        // Fallback: pastagem
        if (this.canGraze && acquireGrazeTarget()) {
            this.activeMode = Mode.GRAZE;
            return true;
        }
        return false;
    }

    private boolean acquireHuntTarget() {
        AABB box = this.dino.getBoundingBox().inflate(SEARCH_RADIUS);
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

    private boolean acquireGroundFood() {
        List<ItemEntity> items = this.dino.level().getEntitiesOfClass(
                ItemEntity.class,
                this.dino.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> item.getItem().is(ModTags.Items.CARNIVORE_FOOD)
                        || item.getItem().is(ModTags.Items.HERBIVORE_FOOD)
        );
        if (items.isEmpty()) return false;

        items.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.groundFoodTarget = items.get(0);
        return true;
    }

    private boolean acquireGrazeTarget() {
        if (!this.canGraze) return false;

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
            case HUNT_ATTACK -> {
                if (this.huntTarget == null || !this.huntTarget.isAlive()
                        || this.dino.distanceToSqr(this.huntTarget) > SEARCH_RADIUS * SEARCH_RADIUS) {
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
            case GRAZE -> {
                if (this.grazeTarget == null
                        || !this.dino.level().getBlockState(this.grazeTarget).is(Blocks.GRASS_BLOCK)) {
                    yield acquireGrazeTarget();
                }
                yield true;
            }
        };
    }

    @Override
    public void start() {
        System.out.println("[QUETZAL DEBUG] OmnivoreHungerGoal START! Mode: " + this.activeMode + " | Dinossauro está voando agora? " + ((AbstractFlyingDinosaurEntity)this.dino).isFlying());

        this.unreachableTicks = 0;
        this.recalcTimer = 0;
        this.attackCooldown = 0;
        this.eatCooldown = 0;
        this.grazeProgress = 0;

        switch (this.activeMode) {
            case HUNT_ATTACK -> {
                if (this.huntTarget != null) {
                    this.dino.getNavigation().moveTo(this.huntTarget, HUNT_SPEED);
                }
            }
            case SEEK_GROUND_FOOD -> {
                if (this.groundFoodTarget != null) {
                    this.dino.getNavigation().moveTo(this.groundFoodTarget, GROUND_FOOD_SPEED);
                }
            }
            case GRAZE -> {
                if (this.grazeTarget != null) {
                    this.dino.getNavigation().moveTo(
                            this.grazeTarget.getX() + 0.5,
                            this.grazeTarget.getY(),
                            this.grazeTarget.getZ() + 0.5,
                            GRAZE_SPEED
                    );
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.dino.tickCount % 20 == 0) {
            System.out.println("[QUETZAL DEBUG] OmnivoreHungerGoal TICK! Mode: " + this.activeMode + " | UnreachableTicks: " + this.unreachableTicks);
        }

        if (this.eatCooldown > 0) this.eatCooldown--;

        switch (this.activeMode) {
            case HUNT_ATTACK -> tickHunt();
            case SEEK_GROUND_FOOD -> tickGroundFood();
            case GRAZE -> tickGraze();
        }
    }

    private void tickHunt() {
        if (this.huntTarget == null) return;

        this.dino.getLookControl().setLookAt(this.huntTarget, 30.0F, 30.0F);

        double distSq = this.dino.distanceToSqr(this.huntTarget);
        double reachLinear = this.dino.getBbWidth() + this.huntTarget.getBbWidth() + 1.5F;
        double reachSq = reachLinear * reachLinear;

        if (this.attackCooldown > 0) this.attackCooldown--;

        if (distSq <= reachSq) {
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
            boolean started = this.dino.getNavigation().moveTo(this.huntTarget, HUNT_SPEED);
            this.recalcTimer = 15;
            this.unreachableTicks = (!started || this.dino.getNavigation().isDone())
                    ? this.unreachableTicks + 1 : 0;
        } else {
            this.recalcTimer--;
            if (this.dino.getNavigation().isDone()) this.unreachableTicks++;
        }
    }

    private void tickGroundFood() {
        if (this.groundFoodTarget == null) return;

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
            this.dino.getNavigation().moveTo(this.groundFoodTarget, GROUND_FOOD_SPEED);
            this.recalcTimer = 15;
        } else {
            this.recalcTimer--;
        }
    }

    private void tickGraze() {
        if (this.grazeTarget == null) return;

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
        this.huntTarget = null;
        this.groundFoodTarget = null;
        this.grazeTarget = null;
        this.grazeProgress = 0;
        this.dino.getNavigation().stop();
    }
}