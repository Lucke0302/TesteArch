package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Trait;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class CuriosityBehaviorGoal<T extends TamableAnimal & FeelingDrivenEntity> extends AbstractFuzzyGoal<T> {
    private final double searchRadius = 16.0D;
    private final double speed = 1.0D;

    private BehaviorResolver.Behavior activeMode;
    private Player targetPlayer;

    public CuriosityBehaviorGoal(T dino) {
        super(dino, Feeling.CURIOSITY, Trait.CURIOSITY);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        this.activeMode = BehaviorResolver.resolve(this.dino, Feeling.CURIOSITY);
        if (this.activeMode == BehaviorResolver.Behavior.DO_NOTHING) return false;

        if (this.activeMode == BehaviorResolver.Behavior.FOLLOW_OWNER_OR_NEAREST
                && this.dino.isTame() && this.dino.getOwner() instanceof Player owner
                && this.dino.distanceToSqr(owner) < searchRadius * searchRadius) {
            this.targetPlayer = owner;
            return true;
        }

        AABB box = this.dino.getBoundingBox().inflate(searchRadius);
        List<Player> players = this.dino.level().getEntitiesOfClass(Player.class, box, p -> !p.isSpectator() && !p.isCreative());
        if (players.isEmpty()) return false;

        players.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.targetPlayer = players.get(0);
        return true;
    }

    @Override
    protected boolean canFuzzyContinue() {
        return this.targetPlayer != null && this.targetPlayer.isAlive()
                && this.dino.distanceToSqr(this.targetPlayer) < searchRadius * searchRadius;
    }

    @Override
    public void start() {
        this.dino.getNavigation().moveTo(this.targetPlayer, speed);
    }

    @Override
    public void tick() {
        this.dino.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
        if (this.dino.distanceToSqr(this.targetPlayer) < 16.0D) {
            this.dino.getNavigation().stop();
            float curCuriosity = this.dino.getFeeling(Feeling.CURIOSITY);
            this.dino.setFeeling(Feeling.CURIOSITY, curCuriosity - 0.005f);
        } else {
            this.dino.getNavigation().moveTo(this.targetPlayer, speed);
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.targetPlayer = null;
        this.dino.getNavigation().stop();
    }
}