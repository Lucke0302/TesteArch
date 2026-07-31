package com.lucas.arch.block.entity;

import com.lucas.arch.entity.QuetzalcoatlusEntity;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

public class QuetzalcoatlusEggBlockEntity extends AbstractDinosaurEggBlockEntity<QuetzalcoatlusEntity> {
    public QuetzalcoatlusEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUETZALCOATLUS_EGG_BE, pos, state);
    }

    @Override protected float getHatchMultiplier() { return 1.1f; }
    @Override protected int getMaxHeatSources() { return 5; }
    @Override protected EntityType<QuetzalcoatlusEntity> getEntityType() { return ModEntities.QUETZALCOATLUS; }
}