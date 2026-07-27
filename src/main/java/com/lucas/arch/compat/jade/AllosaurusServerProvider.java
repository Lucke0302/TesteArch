package com.lucas.arch.compat.jade;

import com.lucas.arch.entity.AgeTier;
import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.PachycephalosaurusEntity;
import com.lucas.arch.entity.Trait;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.TamableAnimal;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.Arrays;
import java.util.List;

public enum AllosaurusServerProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final Identifier ID =
        Identifier.fromNamespaceAndPath("archeology_reimagined", "allosaurus_data");

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof FeelingDrivenEntity feelingDino && accessor.getEntity() instanceof TamableAnimal tamable) {
            
            // Dados genéricos do Motor Emocional
            List<Trait> topTraits = Arrays.stream(Trait.values())
                .sorted((t1, t2) -> Float.compare(feelingDino.getTrait(t2), feelingDino.getTrait(t1)))
                .toList();
            data.putString("PrimaryTrait", topTraits.get(0).name());
            data.putString("SecondaryTrait", topTraits.get(1).name());

            byte dominantState = feelingDino.getDominantState();
            data.putByte("DominantState", dominantState);
            if (dominantState > 0 && dominantState <= Feeling.values().length) {
                Feeling dominantFeeling = Feeling.values()[dominantState - 1];
                data.putFloat("DominantStateValue", feelingDino.getFeeling(dominantFeeling));
            }

            // Recupera especificidades biológicas 
            if (tamable instanceof AllosaurusEntity allo) {
                data.putString("AgeTier", allo.getAgeTier().name());
                data.putBoolean("IsMale", allo.isMale());
                if (allo.getAgeTier() != AgeTier.ADULT) data.putInt("GrowthPercent", allo.getGrowthPercent());
            } else if (tamable instanceof PachycephalosaurusEntity pachy) {
                data.putString("AgeTier", pachy.getAgeTier().name());
                data.putBoolean("IsMale", pachy.isMale());
                if (pachy.getAgeTier() != AgeTier.ADULT) data.putInt("GrowthPercent", pachy.getGrowthPercent());
            }
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}