package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FuzzyCuriosityGoal extends AbstractFuzzyGoal {
    private final double searchRadius = 16.0D;
    private final double speedModifier = 1.0D; 
    private Player targetPlayer;

    public FuzzyCuriosityGoal(AllosaurusEntity dino) {
        super(dino, Feeling.CURIOSITY, Trait.CURIOSITY);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    protected boolean canFuzzyActivate() {
        AABB searchBox = this.dino.getBoundingBox().inflate(searchRadius);
        List<Player> players = this.dino.level().getEntitiesOfClass(Player.class, searchBox, p -> !p.isSpectator() && !p.isCreative());
        
        if (players.isEmpty()) return false;
        
        players.sort((a, b) -> Double.compare(this.dino.distanceToSqr(a), this.dino.distanceToSqr(b)));
        this.targetPlayer = players.get(0);
        return true;
    }

    @Override
    protected boolean canFuzzyContinue() {
        return this.targetPlayer != null && this.targetPlayer.isAlive() && this.dino.distanceToSqr(this.targetPlayer) < (searchRadius * searchRadius);
    }

    @Override
    public void start() {
        this.dino.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
    }

    @Override
    public void tick() {
        this.dino.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);
        
        double distToTargetSqr = this.dino.distanceToSqr(this.targetPlayer);
        
        if (distToTargetSqr < 16.0D) {
            this.dino.getNavigation().stop();
            
            float currentCuriosity = this.dino.getFeeling(Feeling.CURIOSITY);
            this.dino.setFeeling(Feeling.CURIOSITY, currentCuriosity - 0.005f);
        } else {
            this.dino.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
        }
    }

    @Override
    protected void onFuzzyStop() {
        this.targetPlayer = null;
        this.dino.getNavigation().stop();
    }
}