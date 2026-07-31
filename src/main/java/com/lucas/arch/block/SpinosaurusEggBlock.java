package com.lucas.arch.block;

import com.lucas.arch.block.entity.SpinosaurusEggBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpinosaurusEggBlock extends AbstractDinosaurEggBlock {

    public SpinosaurusEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isValidBaseBlock(BlockState state) {
        return super.isValidBaseBlock(state) || state.is(Blocks.SAND); 
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpinosaurusEggBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, bState, t) -> {
            if (t instanceof SpinosaurusEggBlockEntity entity) {
                entity.serverTick(lvl, pos, bState);
            }
        };
    }
}