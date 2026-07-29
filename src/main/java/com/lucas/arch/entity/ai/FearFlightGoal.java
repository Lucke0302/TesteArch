package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AbstractFlyingDinosaurEntity;
import com.lucas.arch.entity.Feeling;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class FearFlightGoal extends Goal {
    private final AbstractFlyingDinosaurEntity entity;
    private Vec3 targetPos;
    private int landingTicks = 0;

    public FearFlightGoal(AbstractFlyingDinosaurEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!entity.isAlive() || entity.isSleeping() || entity.isResting()) return false;
        byte dom = entity.getDominantState();
        return (dom > 0 && Feeling.values()[dom - 1] == Feeling.FEAR)
                || entity.getFeeling(Feeling.STRESS) >= 0.6f;
    }

    @Override
    public boolean canContinueToUse() {
        if (!entity.isAlive() || entity.isSleeping()) return false;
        return entity.isFlying() || landingTicks > 0;
    }

    @Override
    public void start() {
        entity.setFlying(true);
        targetPos = null;
        landingTicks = 0;
    }

    @Override
    public void stop() {
        if (!this.entity.isFlying()) return;
        if (this.entity.hasDiveAnimation()) {
            this.entity.startDiving();
        } else {
            this.entity.setFlying(false);
        }
    }

    @Override
    public void tick() {
        if (!entity.isFlying()) {
            // pousando
            if (landingTicks > 0) {
                performLanding();
                landingTicks--;
            } else {
                entity.setFlying(false);
            }
            return;
        }

        if (targetPos == null || entity.distanceToSqr(targetPos) < 4.0) {
            targetPos = findFearTarget();
        }
        if (targetPos != null) {
            entity.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.2);
        }
    }

    private Vec3 findFearTarget() {
        double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 32;
        double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 32;
        double y = entity.blockPosition().getY() + entity.getFlightAltitude() + entity.getRandom().nextDouble() * 4;
        y = Math.min(y, entity.level().getMaxY() - 5);
        return new Vec3(x, y, z);
    }

    private void performLanding() {
        Vec3 pos = entity.position();
        double targetY = entity.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, entity.blockPosition()).getY() + 1;
        if (pos.y > targetY + 0.5) {
            entity.getNavigation().moveTo(pos.x, targetY, pos.z, 0.8);
        }
    }
}