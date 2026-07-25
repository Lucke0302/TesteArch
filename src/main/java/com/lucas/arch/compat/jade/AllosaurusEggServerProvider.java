package com.lucas.arch.compat.jade;

import com.lucas.arch.block.entity.AllosaurusEggBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum AllosaurusEggServerProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final Identifier ID =
        Identifier.fromNamespaceAndPath("archeology_reimagined", "allosaurus_egg");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof AllosaurusEggBlockEntity eggBE) {
            data.putInt("HatchProgress", eggBE.getHatchProgress());
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}