package com.lucas.arch.block;

import com.lucas.arch.block.entity.QuetzalcoatlusEggBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuetzalcoatlusEggBlock extends AbstractDinosaurEggBlock {

    public QuetzalcoatlusEggBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuetzalcoatlusEggBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, bState, t) -> {
            if (t instanceof QuetzalcoatlusEggBlockEntity entity) {
                entity.serverTick(lvl, pos, bState);
            }
        };
    }
}