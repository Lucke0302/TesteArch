package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AbstractDinosaurEntity;
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
 * (nenhum feeling dominante acima do threshold). O comportamento depende da Trait
 * mais alta do dinossauro:
 *
 * - CURIOSITY: wander aleatório pelo espaço
 * - COWARDICE: sem fome → deita parado; com fome → inquieto, wander
 * - GLUTTONY: sem fome → deita parado; com fome → inquieto, wander
 * - AGGRESSIVENESS: players não-dono a ≤10 blocos → rosna e ativa ANGER; senão → deita
 */
public class NeutralBehaviorGoal<T extends TamableAnimal & FeelingDrivenEntity> extends Goal {
    private final T dino;
    private int cooldown = 0;
    private int wanderTicks = 0;
    private static final int CHECK_INTERVAL = 40; // checa a cada 2 segundos

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

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Continua enquanto neutro e não dormindo
        if (this.dino.getDominantState() != 0) return false;
        if (this.dino instanceof AbstractDinosaurEntity ad && ad.isSleeping()) return false;

        Trait dominant = getDominantTrait();

        // AGGRESSIVENESS: continua enquanto houver player não-dono no raio
        if (dominant == Trait.AGGRESSIVENESS) {
            return isNonOwnerPlayerInRange(10.0);
        }

        // COWARDICE / GLUTTONY: se está deitado, fica deitado
        if ((dominant == Trait.COWARDICE || dominant == Trait.GLUTTONY) && !isHungry()) {
            return true; // descansando
        }

        return this.wanderTicks > 0;
    }

    @Override
    public void start() {
        this.wanderTicks = 0;
        setResting(false);

        Trait dominant = getDominantTrait();

        switch (dominant) {
            case AGGRESSIVENESS -> {
                if (isNonOwnerPlayerInRange(10.0)) {
                    // Rosna e ativa ANGER para transicionar pro AngerBehaviorGoal
                    if (this.dino instanceof AbstractDinosaurEntity ad) {
                        growl();
                    }
                    float currentAnger = this.dino.getFeeling(Feeling.ANGER);
                    this.dino.setFeeling(Feeling.ANGER, Math.min(1.0f, currentAnger + 0.5f));
                } else {
                    setResting(true);
                    this.dino.getNavigation().stop();
                }
            }
            case COWARDICE, GLUTTONY -> {
                if (!isHungry()) {
                    setResting(true);
                    this.dino.getNavigation().stop();
                } else {
                    // Inquieto: wander por aí
                    this.wanderTicks = 100 + this.dino.getRandom().nextInt(100);
                    this.dino.getNavigation().moveTo(
                            this.dino.getX() + (this.dino.getRandom().nextDouble() - 0.5) * 10.0,
                            this.dino.getY(),
                            this.dino.getZ() + (this.dino.getRandom().nextDouble() - 0.5) * 10.0,
                            1.0D
                    );
                }
            }
            case CURIOSITY -> {
                // Wander aleatório pelo espaço
                this.wanderTicks = 100 + this.dino.getRandom().nextInt(100);
                this.dino.getNavigation().moveTo(
                        this.dino.getX() + (this.dino.getRandom().nextDouble() - 0.5) * 14.0,
                        this.dino.getY(),
                        this.dino.getZ() + (this.dino.getRandom().nextDouble() - 0.5) * 14.0,
                        1.0D
                );
            }
        }
    }

    @Override
    public void tick() {
        Trait dominant = getDominantTrait();

        if (dominant == Trait.AGGRESSIVENESS) {
            // Checa periodicamente se ainda tem players no raio
            if (this.dino.tickCount % CHECK_INTERVAL == 0) {
                if (isNonOwnerPlayerInRange(10.0)) {
                    growl();
                    float currentAnger = this.dino.getFeeling(Feeling.ANGER);
                    this.dino.setFeeling(Feeling.ANGER, Math.min(1.0f, currentAnger + 0.15f));
                } else {
                    // Sem ameaça, deita
                    setResting(true);
                    this.dino.getNavigation().stop();
                }
            }
            return;
        }

        if (dominant == Trait.CURIOSITY) {
            this.wanderTicks--;
            if (this.wanderTicks <= 0 || this.dino.getNavigation().isDone()) {
                // Escolhe novo ponto
                this.wanderTicks = 100 + this.dino.getRandom().nextInt(100);
                this.dino.getNavigation().moveTo(
                        this.dino.getX() + (this.dino.getRandom().nextDouble() - 0.5) * 14.0,
                        this.dino.getY(),
                        this.dino.getZ() + (this.dino.getRandom().nextDouble() - 0.5) * 14.0,
                        1.0D
                );
            }
        }

        if ((dominant == Trait.COWARDICE || dominant == Trait.GLUTTONY)) {
            if (isHungry()) {
                // Inquieto: continua wander
                setResting(false);
                this.wanderTicks--;
                if (this.wanderTicks <= 0 || this.dino.getNavigation().isDone()) {
                    this.wanderTicks = 100 + this.dino.getRandom().nextInt(100);
                    this.dino.getNavigation().moveTo(
                            this.dino.getX() + (this.dino.getRandom().nextDouble() - 0.5) * 10.0,
                            this.dino.getY(),
                            this.dino.getZ() + (this.dino.getRandom().nextDouble() - 0.5) * 10.0,
                            1.0D
                    );
                }
            } else {
                // Deita
                setResting(true);
                this.dino.getNavigation().stop();
            }
        }
    }

    @Override
    public void stop() {
        setResting(false);
        this.wanderTicks = 0;
        this.cooldown = 0;
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
            ad.playSound(ModSounds.ALLO_AMBIENT); // usando som ambiente como growl placeholder
        }
    }

    private void setResting(boolean resting) {
        if (this.dino instanceof AbstractDinosaurEntity ad) {
            ad.setResting(resting);
        }
    }
}