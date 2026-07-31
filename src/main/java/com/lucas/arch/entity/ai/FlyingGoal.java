package com.lucas.arch.entity.ai;

import java.util.EnumSet;

import com.lucas.arch.entity.AbstractFlyingDinosaurEntity;
import com.lucas.arch.entity.Feeling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Goal de voo para dinossauros voadores (AbstractFlyingDinosaurEntity).
 * Ciclo padrão: Walk/Run no chão.
 * - MEDO dominante: decola imediatamente (fuga aérea), mantém-se voando
 *   enquanto o medo continuar dominante.
 * - Caso contrário (neutro ou qualquer outro sentimento): a cada
 *   CASUAL_FLIGHT_CHECK_INTERVAL ticks, rola UMA chance de decolar por
 *   voo casual.
 *
 * Prioridade: 4 (ver QuetzalcoatlusEntity#registerGoals)
 */
public class FlyingGoal extends Goal {

    private final AbstractFlyingDinosaurEntity entity;

    private Mode mode = Mode.LANDING;
    private Vec3 targetPos = null;

    private static final int CASUAL_FLIGHT_CHECK_INTERVAL = 400;
    private static final float CASUAL_FLIGHT_CHANCE = 0.2f;
    private int nextCasualFlightCheckTick = 0;

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

    private boolean isFearDominant() {
        byte domState = this.entity.getDominantState();
        return domState > 0 && Feeling.values()[domState - 1] == Feeling.FEAR;
    }

    @Override
    public boolean canUse() {
        if (!this.entity.isAlive()) return false;
        if (this.entity.isSleeping()) return false;
        if (this.entity.isResting()) return false;

        if (isFearDominant()) {
            return true;
        }

        if (this.entity.tickCount >= this.nextCasualFlightCheckTick) {
            this.nextCasualFlightCheckTick = this.entity.tickCount + CASUAL_FLIGHT_CHECK_INTERVAL;
            return this.entity.getRandom().nextFloat() < CASUAL_FLIGHT_CHANCE;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.entity.isAlive()) return false;
        if (this.entity.isSleeping()) return false;

        if (this.mode == Mode.LANDING) {
            return this.entity.isFlying();
        }

        if (isFearDominant()) {
            return true;
        }

        if (this.mode == Mode.FLYING && this.entity.tickCount >= this.flyingEndTick) {
            this.mode = Mode.LANDING;
        }

        return true;
    }

    @Override
    public void start() {
        this.mode = Mode.TAKEOFF;
        this.targetPos = null;
        this.entity.setFlying(true);
    }

    @Override
    public void stop() {
        if (this.entity.isFlying()) {
            this.mode = Mode.LANDING;
        } else {
            this.nextCasualFlightCheckTick = this.entity.tickCount + CASUAL_FLIGHT_CHECK_INTERVAL;
        }
        this.targetPos = null;
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

        double yawRad = Math.toRadians(this.entity.getYRot());
        double forwardX = -Math.sin(yawRad) * 1.5;
        double forwardZ = Math.cos(yawRad) * 1.5;
        Vec3 takeoffTarget = new Vec3(this.entity.getX() + forwardX, targetY, this.entity.getZ() + forwardZ);
        this.entity.steerTo(takeoffTarget.x, takeoffTarget.y, takeoffTarget.z, 1.0);
    }

    private void tickFlying() {
        if (targetPos == null || this.entity.distanceToSqr(targetPos) < 16.0) {
            findNewFlyingTarget();
        }

        if (targetPos != null) {
            this.entity.steerTo(targetPos.x, targetPos.y, targetPos.z, 1.0);
        }
    }

    private void findNewFlyingTarget() {
        for (int attempt = 0; attempt < 8; attempt++) {
            double x = this.entity.getX() + (this.entity.getRandom().nextDouble() - 0.5) * 24;
            double z = this.entity.getZ() + (this.entity.getRandom().nextDouble() - 0.5) * 24;

            int groundY = this.entity.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 0, z)).getY();

            double y = groundY + this.entity.getFlightAltitude() + (this.entity.getRandom().nextDouble() - 0.5) * 6;
            y = Math.max(groundY + 2, Math.min(y, this.entity.level().getMaxY() - 5));

            BlockPos targetBlock = BlockPos.containing(x, y, z);
            if (this.entity.level().isEmptyBlock(targetBlock)) {
                targetPos = new Vec3(x, y, z);
                return;
            }
        }

        double fallbackY = Math.min(
            this.entity.getY() + this.entity.getFlightAltitude() * 0.25,
            this.entity.level().getMaxY() - 5
        );
        targetPos = new Vec3(
            this.entity.getX() + (this.entity.getRandom().nextDouble() - 0.5) * 24,
            fallbackY,
            this.entity.getZ() + (this.entity.getRandom().nextDouble() - 0.5) * 24
        );
    }

    private void tickLanding() {
        if (!this.entity.isFlying()) {
            return;
        }

        int groundY = this.entity.level().getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING,
            this.entity.blockPosition()
        ).getY();

        if (this.entity.getY() <= groundY + 0.5) {
            this.entity.setFlying(false);
            this.entity.getNavigation().stop();
            return;
        }

        double nextY = Math.max(groundY, this.entity.getY() - 2);
        Vec3 landTarget = new Vec3(this.entity.getX(), nextY, this.entity.getZ());
        this.entity.steerTo(landTarget.x, landTarget.y, landTarget.z, 0.8);
    }

    private void scheduleFlyingEnd() {
        this.flyingEndTick = this.entity.tickCount + FLYING_DURATION_MIN
            + this.entity.getRandom().nextInt(FLYING_DURATION_MAX - FLYING_DURATION_MIN);
    }
}