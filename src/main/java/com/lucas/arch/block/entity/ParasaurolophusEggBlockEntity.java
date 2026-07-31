package com.lucas.arch.block.entity;

import com.lucas.arch.entity.ParasaurolophusEntity;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ParasaurolophusEggBlockEntity extends AbstractDinosaurEggBlockEntity<ParasaurolophusEntity> {
    public ParasaurolophusEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PARASAUROLOPHUS_EGG_BE, pos, state);
    }

    @Override protected float getHatchMultiplier() { return 0.8f; }
    @Override protected int getMaxHeatSources() { return 4; }
    @Override protected EntityType<ParasaurolophusEntity> getEntityType() { return ModEntities.PARASAUROLOPHUS; }
}