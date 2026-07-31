package com.lucas.arch.block.entity;

import com.lucas.arch.entity.PachycephalosaurusEntity;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PachycephalosaurusEggBlockEntity extends AbstractDinosaurEggBlockEntity<PachycephalosaurusEntity> {
    public PachycephalosaurusEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PACHYCEPHALOSAURUS_EGG_BE, pos, state);
    }

    @Override protected float getHatchMultiplier() { return 0.7f; }
    @Override protected int getMaxHeatSources() { return 4; }
    @Override protected EntityType<PachycephalosaurusEntity> getEntityType() { return ModEntities.PACHYCEPHALOSAURUS; }
}