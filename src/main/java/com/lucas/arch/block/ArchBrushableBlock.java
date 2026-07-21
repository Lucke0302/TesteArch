package com.lucas.arch.block;

import com.lucas.arch.block.entity.ArchBrushableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ArchBrushableBlock extends BrushableBlock {

    public ArchBrushableBlock(SoundEvent brushSound, SoundEvent brushCompletedSound, Properties properties) {
        super(Blocks.AIR, brushSound, brushCompletedSound, properties); 
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArchBrushableBlockEntity(pos, state);
    }
}