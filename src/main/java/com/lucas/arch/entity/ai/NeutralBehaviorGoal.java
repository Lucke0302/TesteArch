package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AbstractDinosaurEntity;
import com.lucas.arch.entity.AbstractFlyingDinosaurEntity;
import com.lucas.arch.entity.AgeTier;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModSounds;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal de comportamento passivo executado quando o dinossauro está em estado NEUTRO
 * (nenhum feeling dominante acima do threshold).
 *
 * O comportamento segue um ciclo faseado entre WANDERING e RESTING, com durações
 * determinadas pelo AgeTier + Traits:
 *
 * - Bebês (BABY/CHILD): andam MUITO mais do que descansam (~80% wander)
 * - JUVENILE: equilíbrio moderado (~60% wander)
 * - ADULT: descansam tanto quanto andam (~50% cada)
 *
 * Traits modulam:
 * - CURIOSITY: aumenta wander time, diminui rest time
 * - COWARDICE: aumenta rest time, diminui wander time
 * - GLUTTONY: restaurar fome reduz rest time
 * - AGGRESSIVENESS: rosna e ativa ANGER se player não-dono a ≤10 blocos
 *
 * Se estiver com fome (HUNGER >= 0.3f), override para wander inquieto até saciar.
 */
public class NeutralBehaviorGoal<T extends TamableAnimal & FeelingDrivenEntity> extends Goal {
    private final T dino;
    private int cooldown = 0;
    private int phaseTimer = 0;
    private Phase currentPhase = Phase.WANDERING;
    private static final int CHECK_INTERVAL = 40;

    private enum Phase {
        WANDERING, RESTING
    }

    public NeutralBehaviorGoal(T dino) {
        this.dino = dino;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown-- > 0) return false;
        this.cooldown = CHECK_INTERVAL;

        // Só ativa em estado neutro (nenhum feeling dominante)
        if (this.dino.getDominantState() != 0) return false;

        // Se estiver dormindo (tranquilizante), não interfere
        if (this.dino instanceof AbstractDinosaurEntity ad && ad.isSleeping()) return false;

        if (this.dino instanceof AbstractFlyingDinosaurEntity afd && afd.isFlying()) return false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.dino.getDominantState() != 0) return false;
        if (this.dino instanceof AbstractDinosaurEntity ad && ad.isSleeping()) return false;
        
        return true;
    }

    @Override
    public void start() {
        this.phaseTimer = 0;
        setResting(false);
        
        startWanderPhase();
    }

    @Override
    public void tick() {
        Trait dominant = getDominantTrait();

        if (currentPhase == Phase.RESTING) {
            if (this.dino instanceof AbstractDinosaurEntity ad && !ad.canRest()) {
                startWanderPhase();
                return;
            }
        }
        
        if (dominant == Trait.AGGRESSIVENESS) {
            if (this.dino.tickCount % CHECK_INTERVAL == 0) {
                if (isNonOwnerPlayerInRange(10.0)) {
                    growl();
                    float currentAnger = this.dino.getFeeling(Feeling.ANGER);
                    this.dino.setFeeling(Feeling.ANGER, Math.min(1.0f, currentAnger + 0.15f));
                }
            }
        }
        
        if (isHungry()) {
            if (currentPhase != Phase.WANDERING) {
                currentPhase = Phase.WANDERING;
                setResting(false);
            }
            wanderTick();
            return;
        }
        
        this.phaseTimer--;
        if (this.phaseTimer <= 0) {
            switch (currentPhase) {
                case WANDERING -> startRestPhase();
                case RESTING -> startWanderPhase();
            }
        } else {
            switch (currentPhase) {
                case WANDERING -> wanderTick();
                case RESTING -> restTick();
            }
        }
    }

    @Override
    public void stop() {
        setResting(false);
        this.currentPhase = Phase.WANDERING;
        this.phaseTimer = 0;
        this.cooldown = 0;
    }

    // --- Gerenciamento de fases ---

    private void startWanderPhase() {
        currentPhase = Phase.WANDERING;
        setResting(false);
        this.phaseTimer = getWanderDuration();
        wanderTick();
    }

    private void startRestPhase() {
        if (this.dino instanceof AbstractDinosaurEntity ad) {
            if (!ad.canRest()) {
                startWanderPhase();
                return;
            }
        }
        currentPhase = Phase.RESTING;
        setResting(true);
        this.dino.getNavigation().stop();
        this.phaseTimer = getRestDuration();
    }

    private void wanderTick() {
        if (this.dino.getNavigation().isDone() || this.dino.getRandom().nextInt(20) == 0) {
            double range = getWanderRange();
            this.dino.getNavigation().moveTo(
                    this.dino.getX() + (this.dino.getRandom().nextDouble() - 0.5) * range,
                    this.dino.getY(),
                    this.dino.getZ() + (this.dino.getRandom().nextDouble() - 0.5) * range,
                    1.0D
            );
        }
    }

    private void restTick() {
        // Quando descansando, só fica parado — a animação cuida do visual
    }

    private void handleAggressiveTick() {
        if (this.dino.tickCount % CHECK_INTERVAL == 0) {
            if (isNonOwnerPlayerInRange(10.0)) {
                growl();
                float currentAnger = this.dino.getFeeling(Feeling.ANGER);
                this.dino.setFeeling(Feeling.ANGER, Math.min(1.0f, currentAnger + 0.15f));
            } else {
                this.currentPhase = Phase.RESTING;
                setResting(true);
                this.dino.getNavigation().stop();
            }
        }
    }

    // --- Cálculo de durações baseado em AgeTier + Traits ---

    private int getWanderDuration() {
        AgeTier age = getAgeTier();
        Trait dominant = getDominantTrait();
        float traitMod = 1.0f;

        if (dominant == Trait.CURIOSITY) {
            traitMod += getTraitValue(dominant) * 0.5f; // curioso anda mais tempo
        }
        if (dominant == Trait.COWARDICE) {
            traitMod -= getTraitValue(dominant) * 0.3f; // medroso foge logo → descansa mais cedo
        }

        int base = switch (age) {
            case BABY -> 250;
            case CHILD -> 220;
            case JUVENILE -> 180;
            case ADULT -> 140;
        };

        return (int) ((base + this.dino.getRandom().nextInt(80)) * traitMod);
    }

    private int getRestDuration() {
        AgeTier age = getAgeTier();
        Trait dominant = getDominantTrait();
        float traitMod = 1.0f;

        if (dominant == Trait.COWARDICE) {
            traitMod += getTraitValue(dominant) * 0.5f; // medroso descansa mais
        }
        if (dominant == Trait.CURIOSITY) {
            traitMod -= getTraitValue(dominant) * 0.3f; // curioso descansa menos
        }

        int base = switch (age) {
            case BABY -> 50;
            case CHILD -> 70;
            case JUVENILE -> 110;
            case ADULT -> 150;
        };

        return Math.max(20, (int) ((base + this.dino.getRandom().nextInt(40)) * traitMod));
    }

    private double getWanderRange() {
        AgeTier age = getAgeTier();
        return switch (age) {
            case BABY -> 6.0;   // Bebês andam perto
            case CHILD -> 8.0;
            case JUVENILE -> 10.0;
            case ADULT -> 12.0; // Adultos exploram mais longe
        };
    }

    // --- Helpers ---

    private Trait getDominantTrait() {
        Trait dominant = Trait.CURIOSITY;
        float highest = 0;
        for (Trait t : Trait.values()) {
            float val = this.dino.getTrait(t);
            if (val > highest) {
                highest = val;
                dominant = t;
            }
        }
        return dominant;
    }

    private float getTraitValue(Trait trait) {
        return this.dino.getTrait(trait);
    }

    private AgeTier getAgeTier() {
        if (this.dino instanceof AbstractDinosaurEntity ad) {
            return ad.getAgeTier();
        }
        return AgeTier.ADULT;
    }

    private boolean isHungry() {
        return this.dino.getFeeling(Feeling.HUNGER) >= 0.3f;
    }

    private boolean isNonOwnerPlayerInRange(double radius) {
        AABB box = this.dino.getBoundingBox().inflate(radius);
        List<Player> players = this.dino.level().getEntitiesOfClass(Player.class, box,
                p -> !p.isCreative() && !p.isSpectator() && !this.dino.isOwnedBy(p));
        return !players.isEmpty();
    }

    private void growl() {
        if (this.dino instanceof AbstractDinosaurEntity ad) {
            ad.playSound(ModSounds.ALLO_AMBIENT);
        }
    }

    private void setResting(boolean resting) {
        if (this.dino instanceof AbstractDinosaurEntity ad) {
            ad.setResting(resting);
        }
    }
}