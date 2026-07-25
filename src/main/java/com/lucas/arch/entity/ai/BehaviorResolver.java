package com.lucas.arch.entity.ai;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;

/**
 * Resolve qual comportamento vence dentro de um Feeling dominante, comparando
 * as duas traits de maior valor no dino via tabela de confronto par-a-par.
 * NÃO é uma ordem global (a tabela de CURIOSITY não é transitiva por design),
 * então a resolução é sempre par-a-par, nunca por ranking acumulado.
 */
public final class BehaviorResolver {

    public enum Behavior {
        HUNT_ATTACK,
        FLEE,
        SEEK_GROUND_FOOD,
        BEG_OWNER,
        FOLLOW_PLAYER,
        FOLLOW_OWNER_OR_NEAREST,
        DO_NOTHING
    }

    private BehaviorResolver() {}

    public static Behavior resolve(AllosaurusEntity dino, Feeling feeling) {
        return mapTraitToBehavior(feeling, resolveDominantTrait(dino, feeling));
    }

    private static Trait resolveDominantTrait(AllosaurusEntity dino, Feeling feeling) {
        Trait[] all = Trait.values();
        Trait first = all[0];
        Trait second = all[1];
        for (Trait t : all) {
            if (dino.getTrait(t) > dino.getTrait(first)) {
                second = first;
                first = t;
            } else if (t != first && dino.getTrait(t) > dino.getTrait(second)) {
                second = t;
            }
        }
        return resolvePair(dino, feeling, first, second);
    }

    private static Trait resolvePair(AllosaurusEntity dino, Feeling feeling, Trait a, Trait b) {
        if (a == b) return a;
        Trait winner = switch (feeling) {
            case HUNGER -> resolveHunger(a, b);
            case FEAR -> resolveFear(a, b);
            case ANGER -> resolveAnger(a, b);
            case CURIOSITY -> resolveCuriosity(a, b);
        };
        if (winner != null) return winner;

        float va = dino.getTrait(a);
        float vb = dino.getTrait(b);
        if (va > vb) return a;
        if (vb > va) return b;
        return dino.getRandom().nextBoolean() ? a : b;
    }

    private static Trait resolveHunger(Trait a, Trait b) {
        return higherOf(a, b, Trait.AGGRESSIVENESS, Trait.GLUTTONY, Trait.COWARDICE, Trait.CURIOSITY);
    }

    private static Trait resolveFear(Trait a, Trait b) {
        if (isPair(a, b, Trait.CURIOSITY, Trait.GLUTTONY)) return null;
        return higherOf(a, b, Trait.COWARDICE, Trait.AGGRESSIVENESS, Trait.CURIOSITY, Trait.GLUTTONY);
    }

    private static Trait resolveAnger(Trait a, Trait b) {
        return higherOf(a, b, Trait.AGGRESSIVENESS, Trait.GLUTTONY, Trait.CURIOSITY, Trait.COWARDICE);
    }

    private static Trait resolveCuriosity(Trait a, Trait b) {
        if (isPair(a, b, Trait.AGGRESSIVENESS, Trait.CURIOSITY)) return Trait.CURIOSITY;
        if (isPair(a, b, Trait.AGGRESSIVENESS, Trait.COWARDICE)) return Trait.COWARDICE;
        if (isPair(a, b, Trait.AGGRESSIVENESS, Trait.GLUTTONY)) return null; // empate
        if (isPair(a, b, Trait.COWARDICE, Trait.CURIOSITY)) return Trait.CURIOSITY;
        if (isPair(a, b, Trait.COWARDICE, Trait.GLUTTONY)) return Trait.GLUTTONY;
        if (isPair(a, b, Trait.CURIOSITY, Trait.GLUTTONY)) return Trait.CURIOSITY;
        return null;
    }

    private static boolean isPair(Trait a, Trait b, Trait x, Trait y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    private static Trait higherOf(Trait a, Trait b, Trait... orderHighToLow) {
        for (Trait t : orderHighToLow) {
            if (a == t) return a;
            if (b == t) return b;
        }
        return a;
    }

    private static Behavior mapTraitToBehavior(Feeling feeling, Trait winner) {
        return switch (feeling) {
            case HUNGER -> switch (winner) {
                case AGGRESSIVENESS -> Behavior.HUNT_ATTACK;
                case COWARDICE, CURIOSITY -> Behavior.SEEK_GROUND_FOOD;
                case GLUTTONY -> Behavior.BEG_OWNER;
            };
            case FEAR -> switch (winner) {
                case AGGRESSIVENESS -> Behavior.HUNT_ATTACK;
                case COWARDICE, CURIOSITY, GLUTTONY -> Behavior.FLEE;
            };
            case ANGER -> switch (winner) {
                case AGGRESSIVENESS, CURIOSITY, GLUTTONY -> Behavior.HUNT_ATTACK;
                case COWARDICE -> Behavior.FLEE;
            };
            case CURIOSITY -> switch (winner) {
                case AGGRESSIVENESS, GLUTTONY -> Behavior.FOLLOW_PLAYER;
                case COWARDICE -> Behavior.DO_NOTHING;
                case CURIOSITY -> Behavior.FOLLOW_OWNER_OR_NEAREST;
            };
        };
    }
}