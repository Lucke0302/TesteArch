package com.lucas.arch.block;

import com.lucas.arch.world.gen.CycadFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CycadSaplingBlock extends BushBlock {

    public CycadSaplingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BONE_MEAL)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                if (serverLevel.getRandom().nextFloat() < 0.50f) {
                    CycadFeature.generate(serverLevel, pos);
                } else {
                    serverLevel.levelEvent(1505, pos, 0);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}