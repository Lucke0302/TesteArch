package com.lucas.arch.block;

import com.lucas.arch.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.gameevent.GameEvent;

public class BitterBerryBushBlock extends SweetBerryBushBlock {
    
    public BitterBerryBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        boolean isFullyGrown = age == 3;
        
        if (!isFullyGrown && player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        } else if (age > 1) { 
            int dropCount = 1 + level.getRandom().nextInt(2);
            popResource(level, pos, new ItemStack(ModItems.BITTER_BERRIES, dropCount + (isFullyGrown ? 1 : 0)));
            
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            
            BlockState resetState = state.setValue(AGE, 1);
            level.setBlock(pos, resetState, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, resetState));
            
            return InteractionResult.SUCCESS;
        } else {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);

        if (!(entity instanceof LivingEntity)) return;

        entity.makeStuckInBlock(state, new Vec3(0.8D, 0.75D, 0.8D));
        
        if (!level.isClientSide() && state.getValue(AGE) > 0 && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            double deltaX = Math.abs(entity.getX() - entity.xOld);
            double deltaZ = Math.abs(entity.getZ() - entity.zOld);
            
            if (deltaX >= 0.003D || deltaZ >= 0.003D) {
                entity.hurt(level.damageSources().sweetBerryBush(), 1.0F); 
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1)); 
            }
        }
    }
}