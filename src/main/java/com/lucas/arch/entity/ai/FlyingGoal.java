package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AbstractFlyingDinosaurEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal de voo para dinossauros voadores (AbstractFlyingDinosaurEntity).
 * - Estado neutro: decola periodicamente (~a cada 30-60s)
 * - FEAR dominante: decola imediatamente (fuga aérea)
 * - STRESS >= 0.6f: fica voando obsessivamente
 *
 * Prioridade: 0 (antes do NeutralBehaviorGoal)
 */
public class FlyingGoal extends Goal {

    private final AbstractFlyingDinosaurEntity entity;
    private int nextTakeoffTick = 0;
    private Mode mode = Mode.LANDING;
    private Vec3 targetPos = null;
    private static final int TAKEOFF_COOLDOWN_MIN = 600;
    private static final int TAKEOFF_COOLDOWN_MAX = 1200;
    private static final int FLYING_DURATION_MIN = 200;
    private static final int FLYING_DURATION_MAX = 800;
    private int flyingEndTick = 0;

    private enum Mode {
        TAKEOFF, FLYING, LANDING
    }

    public FlyingGoal(AbstractFlyingDinosaurEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.entity.isAlive()) return false;
        if (this.entity.isSleeping()) return false;
        if (this.entity.isResting()) return false;

        byte domState = this.entity.getDominantState();

        if (domState > 0 && Feeling.values()[domState - 1] == Feeling.FEAR) {
            return true;
        }

        if (this.entity.getFeeling(Feeling.STRESS) >= 0.6f) {
            return true;
        }

        if (domState == 0 && this.entity.tickCount >= this.nextTakeoffTick) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.entity.isAlive()) return false;
        if (this.entity.isSleeping()) return false;

        byte domState = this.entity.getDominantState();
        if (domState > 0) {
            Feeling dominant = Feeling.values()[domState - 1];
            if (dominant == Feeling.HUNGER) return false;
            if (dominant == Feeling.ANGER &&
                this.entity.getTrait(Trait.AGGRESSIVENESS) >= this.entity.getTrait(Trait.COWARDICE)) {
                return false;
            }
            if (dominant == Feeling.CURIOSITY) return false;
        }

        if (this.mode == Mode.LANDING && !this.entity.isFlying()) {
            return false;
        }

        if (this.mode == Mode.FLYING && this.entity.tickCount >= this.flyingEndTick) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.mode = Mode.TAKEOFF;
        this.targetPos = null;
        this.entity.setFlying(true);

        if (this.entity.isFlying()) {
            this.mode = Mode.FLYING;
            scheduleFlyingEnd();
        }
    }

    @Override
    public void stop() {
        if (this.entity.isFlying()) {
            this.mode = Mode.LANDING;
        } else {
            this.nextTakeoffTick = this.entity.tickCount + TAKEOFF_COOLDOWN_MIN
                + this.entity.getRandom().nextInt(TAKEOFF_COOLDOWN_MAX - TAKEOFF_COOLDOWN_MIN);
        }
    }

    @Override
    public void tick() {
        switch (this.mode) {
            case TAKEOFF -> tickTakeoff();
            case FLYING -> tickFlying();
            case LANDING -> tickLanding();
        }
    }

    private void tickTakeoff() {
        double targetY = this.entity.blockPosition().getY() + this.entity.getFlightAltitude();
        targetY = Math.min(targetY, this.entity.level().getMaxY() - 5);

        if (this.entity.getY() >= targetY - 1.0) {
            this.mode = Mode.FLYING;
            this.entity.setFlying(true);
            scheduleFlyingEnd();
            return;
        }

        Vec3 takeoffTarget = new Vec3(this.entity.getX(), targetY, this.entity.getZ());
        this.entity.getNavigation().moveTo(takeoffTarget.x, takeoffTarget.y, takeoffTarget.z, 1.0);
    }

    private void tickFlying() {
        if (targetPos == null || this.entity.distanceToSqr(targetPos) < 4.0) {
            findNewFlyingTarget();
        }

        if (targetPos != null) {
            this.entity.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0);
        }
    }

    private void findNewFlyingTarget() {
        double x = this.entity.getX() + (this.entity.getRandom().nextDouble() - 0.5) * 16;
        double y = this.entity.blockPosition().getY() + this.entity.getFlightAltitude()
            + (this.entity.getRandom().nextDouble() - 0.5) * 6;
        double z = this.entity.getZ() + (this.entity.getRandom().nextDouble() - 0.5) * 16;

        y = Math.max(this.entity.blockPosition().getY() + 2, Math.min(y, this.entity.level().getMaxY() - 5));
        targetPos = new Vec3(x, y, z);
    }

    private void tickLanding() {
        if (!this.entity.isFlying()) {
            return;
        }

        double groundY = this.entity.blockPosition().getY();
        if (this.entity.getY() <= groundY + 1.0) {
            this.entity.setFlying(false);
            this.entity.getNavigation().stop();
            return;
        }

        Vec3 landTarget = new Vec3(this.entity.getX(), this.entity.getY() - 2, this.entity.getZ());
        this.entity.getNavigation().moveTo(landTarget.x, landTarget.y, landTarget.z, 0.8);
    }

    private void scheduleFlyingEnd() {
        this.flyingEndTick = this.entity.tickCount + FLYING_DURATION_MIN
            + this.entity.getRandom().nextInt(FLYING_DURATION_MAX - FLYING_DURATION_MIN);
    }
}