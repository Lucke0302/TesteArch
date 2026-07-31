package com.lucas.arch.block.entity;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AllosaurusEggBlockEntity extends AbstractDinosaurEggBlockEntity<AllosaurusEntity> {
    public AllosaurusEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOSAURUS_EGG_BE, pos, state);
    }

    @Override protected float getHatchMultiplier() { return 1.0f; }
    @Override protected int getMaxHeatSources() { return 5; }
    @Override protected EntityType<AllosaurusEntity> getEntityType() { return ModEntities.ALLOSAURUS; }
}