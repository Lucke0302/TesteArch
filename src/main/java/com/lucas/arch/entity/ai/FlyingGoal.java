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
 * - STRESS >= 0.8f: fica voando obsessivamente
 * - HUNGER >= 0.8f: decola para procurar comida (se não houver comida acessível)
 *
 * Prioridade: 0 (antes do NeutralBehaviorGoal)
 */
public class FlyingGoal extends Goal {

    private final AbstractFlyingDinosaurEntity entity;
    private int nextTakeoffTick = 0;
    private Mode mode = Mode.LANDING;
    private Vec3 targetPos = null;
    private static final int TAKEOFF_COOLDOWN_MIN = 1200;
    private static final int TAKEOFF_COOLDOWN_MAX = 2400;
    private static final int FLYING_DURATION_MIN = 200;
    private static final int FLYING_DURATION_MAX = 800;
    private int flyingEndTick = 0;
    private int flightTicks = 0; // para limitar duração

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

        // FEAR dominante
        if (domState > 0 && Feeling.values()[domState - 1] == Feeling.FEAR) {
            return true;
        }

        // STRESS muito alto
        if (this.entity.getFeeling(Feeling.STRESS) >= 0.8f) {
            return true;
        }

        // FOME alta – ativa voo para procurar comida
        if (this.entity.getFeeling(Feeling.HUNGER) >= 0.8f) {
            // Opcional: verificar se há comida nas proximidades (para evitar voo desnecessário)
            // Se não houver comida, voa
            return true;
        }

        // Voo casual em estado neutro
        if (domState == 0 && this.entity.tickCount >= this.nextTakeoffTick) {
            return this.entity.getRandom().nextFloat() < 0.2f;
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
            if (dominant == Feeling.HUNGER) {
                // Se a fome ainda estiver alta, continua voando
                if (this.entity.getFeeling(Feeling.HUNGER) >= 0.5f) {
                    return true;
                }
            }
            if (dominant == Feeling.HUNGER && this.entity.getFeeling(Feeling.HUNGER) < 0.5f) {
                // Se a fome baixou, pode pousar
                return false;
            }
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
            // Se estiver com fome, renova o tempo de voo
            if (this.entity.getFeeling(Feeling.HUNGER) >= 0.6f) {
                scheduleFlyingEnd();
                return true;
            }
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.mode = Mode.TAKEOFF;
        this.targetPos = null;
        this.flightTicks = 0;
        this.entity.setFlying(true);
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

        // Pequeno deslocamento horizontal na direção em que o bicho já está olhando,
        // pra evitar mirar exatamente em cima da própria posição (yaw instável / giro no lugar).
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
        this.entity.steerTo(landTarget.x, landTarget.y, landTarget.z, 0.8);
    }

    private void scheduleFlyingEnd() {
        this.flyingEndTick = this.entity.tickCount + FLYING_DURATION_MIN
            + this.entity.getRandom().nextInt(FLYING_DURATION_MAX - FLYING_DURATION_MIN);
    }
}