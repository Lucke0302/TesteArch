package com.lucas.arch.block;

import com.lucas.arch.block.entity.AbstractDinosaurEggBlockEntity;
import com.lucas.arch.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractDinosaurEggBlock extends Block implements EntityBlock {

    public AbstractDinosaurEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockBelowPos = pos.below();
        BlockState blockBelowState = level.getBlockState(blockBelowPos);
        return isValidBaseBlock(blockBelowState);
    }

    protected boolean isValidBaseBlock(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.HAY_BLOCK);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            
            if (!player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractDinosaurEggBlockEntity<?> eggBE) {
                
                ItemStack dropStack = new ItemStack(this.asItem());
                
                dropStack.set(ModDataComponentTypes.DNA_QUALITY, eggBE.getDnaQuality());
                
                CompoundTag tag = new CompoundTag();
                tag.putInt("HatchProgress", eggBE.getHatchProgress());
                tag.putInt("TickCounter", eggBE.getTickCounter());
                tag.putFloat("RngFactor", eggBE.getRngFactor());
                
                dropStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                
                int progress = eggBE.getHatchProgress();
                if (progress > 0) {
                    dropStack.set(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("Eclosão Salva: " + progress + "%").withStyle(ChatFormatting.GOLD)
                    )));
                }
                
                level.removeBlock(pos, false);
                
                if (!player.getInventory().add(dropStack)) {
                    player.drop(dropStack, false);
                }
                
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F + (level.getRandom().nextFloat() * 0.2F));
            }
        }
        
        return InteractionResult.SUCCESS; 
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractDinosaurEggBlockEntity<?> eggBE) {
                
                if (stack.has(ModDataComponentTypes.DNA_QUALITY)) {
                    eggBE.setDnaQuality(stack.getOrDefault(ModDataComponentTypes.DNA_QUALITY, 50));
                }

                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                if (!customData.isEmpty()) {
                    CompoundTag tag = customData.copyTag();
                    
                    eggBE.setHatchProgress(tag.getInt("HatchProgress").orElse(0));
                    eggBE.setTickCounter(tag.getInt("TickCounter").orElse(0));
                    eggBE.setRngFactor(tag.getFloat("RngFactor").orElse(0.0f));
                }
            }
        }
    }
}