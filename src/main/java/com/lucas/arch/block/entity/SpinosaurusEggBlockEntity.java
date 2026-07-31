package com.lucas.arch.block.entity;

import com.lucas.arch.entity.SpinosaurusEntity;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SpinosaurusEggBlockEntity extends AbstractDinosaurEggBlockEntity<SpinosaurusEntity> {
    public SpinosaurusEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPINOSAURUS_EGG_BE, pos, state);
    }

    @Override protected float getHatchMultiplier() { return 1.2f; }
    @Override protected int getMaxHeatSources() { return 5; }
    @Override protected EntityType<SpinosaurusEntity> getEntityType() { return ModEntities.SPINOSAURUS; }
}