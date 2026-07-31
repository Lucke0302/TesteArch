package com.lucas.arch.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDinosaurEggBlock extends Block implements EntityBlock {

    public AbstractDinosaurEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockBelowPos = pos.below();
        BlockState blockBelowState = level.getBlockState(blockBelowPos);
        
        return isValidBaseBlock(blockBelowState);
    }

    protected boolean isValidBaseBlock(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.HAY_BLOCK);
    }
}