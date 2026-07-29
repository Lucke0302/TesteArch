package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AbstractFlyingDinosaurEntity;
import com.lucas.arch.entity.Feeling;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class CasualFlightGoal extends Goal {
    private final AbstractFlyingDinosaurEntity entity;
    private int nextTakeoffTick = 0;
    private int flightEndTick = 0;
    private Vec3 targetPos;
    private boolean landing = false;

    private static final int COOLDOWN_MIN = 600;
    private static final int COOLDOWN_MAX = 1200;
    private static final int FLIGHT_MIN = 200;
    private static final int FLIGHT_MAX = 600;

    public CasualFlightGoal(AbstractFlyingDinosaurEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!entity.isAlive() || entity.isSleeping() || entity.isResting()) return false;
        if (entity.getDominantState() != 0) return false;
        if (entity.getFeeling(Feeling.FEAR) > 0.3f || entity.getFeeling(Feeling.STRESS) >= 0.6f) return false;
        return entity.tickCount >= nextTakeoffTick;
    }

    @Override
    public boolean canContinueToUse() {
        if (!entity.isAlive() || entity.isSleeping()) return false;
        if (landing) return entity.isFlying();
        if (entity.tickCount >= flightEndTick && entity.isFlying()) {
            landing = true;
            targetPos = null;
            return true;
        }
        return entity.isFlying() && entity.tickCount < flightEndTick;
    }

    @Override
    public void start() {
        entity.setFlying(true);
        landing = false;
        targetPos = null;
        flightEndTick = entity.tickCount + FLIGHT_MIN + entity.getRandom().nextInt(FLIGHT_MAX - FLIGHT_MIN);
    }

    @Override
    public void stop() {
        nextTakeoffTick = entity.tickCount + COOLDOWN_MIN + entity.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (landing) {
            performLanding();
            return;
        }

        if (targetPos == null || entity.distanceToSqr(targetPos) < 4.0) {
            targetPos = findFlightTarget();
        }
        if (targetPos != null) {
            entity.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0);
        }
    }

    private Vec3 findFlightTarget() {
        double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 24;
        double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 24;
        double y = entity.blockPosition().getY() + entity.getFlightAltitude() + entity.getRandom().nextDouble() * 4;
        y = Math.min(y, entity.level().getMaxY() - 5);
        return new Vec3(x, y, z);
    }

    private void performLanding() {
        if (this.entity.isFlying() && !this.entity.isDiving()) {
            if (this.entity.hasDiveAnimation()) {
                this.entity.startDiving();
                return;
            } else {
                this.entity.setFlying(false);
                this.entity.getNavigation().stop();
                this.landing = false;
                return;
            }
        }

        if (this.entity.isDiving()) {
            return;
        }

        this.landing = false;
    }
}