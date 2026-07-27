package com.lucas.arch.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.registry.ModDataComponentTypes;

public class EmptySyringeItem extends ArchItem {
    public EmptySyringeItem(Item.Properties properties, String designer, String programmer) {
        super(properties, designer, programmer);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof FeelingDrivenEntity dino && entity instanceof TamableAnimal tamable) {
            if (!player.level().isClientSide()) {
                boolean isOwner = tamable.isOwnedBy(player);
                
                if (!isOwner) {
                    float aggroTrait = dino.getTrait(Trait.AGGRESSIVENESS);
                    float currentAnger = dino.getFeeling(Feeling.ANGER);
                    
                    dino.setFeeling(Feeling.ANGER, currentAnger + aggroTrait);
                    
                    if (aggroTrait >= 0.7f) {
                        tamable.setTarget(player);
                    }
                }

                // 50% de chance de extração
                if (player.getRandom().nextFloat() <= 0.5f) {
                    ItemStack bloodSyringe = new ItemStack(ModItems.BLOOD_SYRINGE);
                    Identifier speciesId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    bloodSyringe.set(ModDataComponentTypes.SYRINGE_SPECIES, speciesId);
                    
                    stack.shrink(1);
                    if (!player.getInventory().add(bloodSyringe)) {
                        player.drop(bloodSyringe, false);
                    }
                } else {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}