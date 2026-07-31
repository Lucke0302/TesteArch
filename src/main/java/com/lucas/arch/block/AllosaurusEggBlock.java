package com.lucas.arch.block;

import com.lucas.arch.block.entity.AllosaurusEggBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AllosaurusEggBlock extends AbstractDinosaurEggBlock {

    public AllosaurusEggBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AllosaurusEggBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, bState, t) -> {
            if (t instanceof AllosaurusEggBlockEntity entity) {
                entity.serverTick(lvl, pos, bState);
            }
        };
    }
}