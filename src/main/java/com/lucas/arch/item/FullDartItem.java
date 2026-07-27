package com.lucas.arch.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.lucas.arch.entity.AbstractDinosaurEntity;

public class FullDartItem extends ArchItem {
    public FullDartItem(Item.Properties properties, String designer, String programmer) {
        super(properties, designer, programmer);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof AbstractDinosaurEntity dino && !dino.isSleeping()) {
            if (!player.level().isClientSide()) {
                dino.addDartDose();
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}