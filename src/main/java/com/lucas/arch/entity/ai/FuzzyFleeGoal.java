package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class FuzzyFleeGoal extends AbstractFuzzyGoal {
    private final double searchRadius = 16.0D;
    private final double speedModifier = 1.5D; 
    private LivingEntity threat;
    private double runX;
    private double runY;
    private double runZ;

    public FuzzyFleeGoal(AllosaurusEntity dino) {
        super(dino, Feeling.FEAR, Trait.COWARDICE);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    protected boolean canFuzzyActivate() {
        AABB searchBox = this.dino.getBoundingBox().inflate(searchRadius);
        
        List<LivingEntity> entities = this.dino.level().getEntitiesOfClass(LivingEntity.class, searchBox, e ->
            (e instanceof Player player && (!player.isCreative() && !player.isSpectator())) ||
            (e.getType() == this.dino.getType() && e != this.dino)
        );

        if (entities.isEmpty()) return false;

        entities.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.threat = entities.get(0);

        Vec3 fleePos = DefaultRandomPos.getPosAway(this.dino, 16, 7, this.threat.position());
        
        if (fleePos == null) {
            return false;
        } else if (this.threat.distanceToSqr(fleePos.x, fleePos.y, fleePos.z) < this.threat.distanceToSqr(this.dino)) {
            return false;
        } else {
            this.runX = fleePos.x;
            this.runY = fleePos.y;
            this.runZ = fleePos.z;
            return true;
        }
    }

    @Override
    protected boolean canFuzzyContinue() {
        return !this.dino.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.dino.getNavigation().moveTo(this.runX, this.runY, this.runZ, this.speedModifier);
    }

    @Override
    protected void onFuzzyStop() {
        this.threat = null;
        this.dino.getNavigation().stop();
    }
}