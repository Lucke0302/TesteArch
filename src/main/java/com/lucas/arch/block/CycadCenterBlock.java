package com.lucas.arch.block;

import com.lucas.arch.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class CycadCenterBlock extends Block {

    public static final BooleanProperty HAS_FRUIT = BooleanProperty.create("has_fruit");

    public CycadCenterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_FRUIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_FRUIT);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BONE_MEAL) && !state.getValue(HAS_FRUIT)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                if (serverLevel.getRandom().nextFloat() < 0.50f) {
                    level.setBlock(pos, state.setValue(HAS_FRUIT, true), 3);
                    level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    serverLevel.levelEvent(1505, pos, 0);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(HAS_FRUIT)) {
            if (!level.isClientSide()) {
                int count = 1 + level.getRandom().nextInt(2);
                popResource(level, pos, new ItemStack(ModItems.CYCAD_FRUIT, count));

                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F);
                level.setBlock(pos, state.setValue(HAS_FRUIT, false), 3);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}