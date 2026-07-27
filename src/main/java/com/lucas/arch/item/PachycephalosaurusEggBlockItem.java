package com.lucas.arch.item;

import com.lucas.arch.block.entity.PachycephalosaurusEggBlockEntity;
import com.lucas.arch.registry.ModDataComponentTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PachycephalosaurusEggBlockItem extends ArchBlockItem {
    
    public PachycephalosaurusEggBlockItem(Block block, Properties properties, String designer, String programmer) {
        super(block, properties, designer, programmer);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && !context.getLevel().isClientSide()) {
            BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
            if (be instanceof PachycephalosaurusEggBlockEntity eggBE) {
                int quality = context.getItemInHand().getOrDefault(ModDataComponentTypes.DNA_QUALITY, 50);
                eggBE.setDnaQuality(quality);
            }
        }
        return placed;
    }
}