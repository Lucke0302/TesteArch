package com.lucas.arch.compat.jade;

import com.lucas.arch.entity.AgeTier;
import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.Trait;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
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
        if (accessor.getEntity() instanceof AllosaurusEntity allo) {
            List<Trait> topTraits = Arrays.stream(Trait.values())
                .sorted((t1, t2) -> Float.compare(allo.getTrait(t2), allo.getTrait(t1)))
                .toList();

            data.putString("PrimaryTrait", topTraits.get(0).name());
            data.putString("SecondaryTrait", topTraits.get(1).name());
            data.putByte("DominantState", allo.getDominantState());
            data.putString("AgeTier", allo.getAgeTier().name());
            
            data.putBoolean("IsMale", allo.isMale());

            if (allo.getAgeTier() != AgeTier.ADULT) {
                data.putInt("GrowthPercent", allo.getGrowthPercent());
            }
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}