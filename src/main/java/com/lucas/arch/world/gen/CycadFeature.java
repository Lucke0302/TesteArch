package com.lucas.arch.world.gen;

import com.lucas.arch.block.CycadCenterBlock;
import com.lucas.arch.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CycadFeature {

    public static boolean generate(ServerLevel level, BlockPos pos) {
        BlockPos centerTop = pos.above();

        level.setBlock(pos, ModBlocks.CYCAD_LOG.defaultBlockState(), 3);

        level.setBlock(centerTop, ModBlocks.CYCAD_CENTER.defaultBlockState().setValue(CycadCenterBlock.HAS_FRUIT, true), 3);
        BlockState leafSlab = Blocks.OAK_SLAB.defaultBlockState();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                level.setBlock(centerTop.offset(x, 0, z), leafSlab, 3);
            }
        }

        return true;
    }
}