package com.lucas.arch.block.entity;

import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModBlocks;
import com.lucas.arch.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ArchBrushableBlockEntity extends BrushableBlockEntity {

    public ArchBrushableBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.ARCH_BRUSHABLE_BE;
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return state.is(ModBlocks.BRUSHED_SAND) || state.is(ModBlocks.BRUSHED_GRAVEL) || state.is(ModBlocks.BRUSHED_TUFF);
    }

    @Override
    public boolean brush(long time, ServerLevel serverLevel, LivingEntity player, Direction direction, ItemStack stack) {
        int oldDusted = this.getBlockState().hasProperty(BlockStateProperties.DUSTED) 
            ? this.getBlockState().getValue(BlockStateProperties.DUSTED) : 0;
        
        boolean success = super.brush(time, serverLevel, player, direction, stack);

        int newDusted = this.getBlockState().hasProperty(BlockStateProperties.DUSTED) 
            ? this.getBlockState().getValue(BlockStateProperties.DUSTED) : 0;

        if (newDusted > oldDusted) {
            float roll = serverLevel.getRandom().nextFloat();

            if (roll < 0.075f) {
                dropRareItem(serverLevel);
                serverLevel.destroyBlock(this.worldPosition, false);
                return success;
            } else {
                dropDustItem(serverLevel);
            }
        }

        return success;
    }

    private void dropRareItem(ServerLevel serverLevel) {
        int rareRoll = serverLevel.getRandom().nextInt(100);
        Item rareLootItem;
        
        if (rareRoll < 22) {
            rareLootItem = ModItems.UNKNOWN_MAMMAL_FOSSIL;
        } else if (rareRoll < 44) {
            rareLootItem = ModItems.UNKNOWN_REPTILE_FOSSIL;
        } else if (rareRoll < 66) {
            rareLootItem = ModItems.UNKNOWN_FISH_FOSSIL;
        } else if (rareRoll < 88) {
            rareLootItem = ModItems.UNKNOWN_PLANT_FOSSIL;
        } else {
            rareLootItem = Items.SNIFFER_EGG;
        }

        Block.popResource(serverLevel, this.worldPosition.above(), new ItemStack(rareLootItem));
    }

    private void dropDustItem(ServerLevel serverLevel) {
        Item dust = ModItems.SAND_POWDER; 
        
        if (this.getBlockState().is(ModBlocks.BRUSHED_GRAVEL)) {
            dust = ModItems.GRAVEL_POWDER;
        } else if (this.getBlockState().is(ModBlocks.BRUSHED_TUFF)) {
            dust = ModItems.TUFF_POWDER;
        }

        Block.popResource(serverLevel, this.worldPosition.above(), new ItemStack(dust, 1));
    }
}