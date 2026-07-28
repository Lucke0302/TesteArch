package com.lucas.arch.item;

import com.lucas.arch.entity.AbstractDinosaurEntity;
import com.lucas.arch.entity.Feeling;
import com.lucas.arch.entity.FeelingDrivenEntity;
import com.lucas.arch.entity.Trait;
import com.lucas.arch.registry.ModItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Dardo tranquilizante. Ao interagir com um dinossauro, aplica uma dose de
 * tranquilizante (addDartDose). O número de doses necessárias é determinado
 * pelo {@code getBbHeight()} do dinossauro.
 * 
 * Se não for o dono, aumenta a raiva proporcional à agressividade da
 * criatura (impacto maior que a seringa para premiar futura arma de dardos).
 * Após o uso, devolve um dardo vazio ao inventário do jogador.
 */
public class FullDartItem extends ArchItem {
    public FullDartItem(Item.Properties properties, String designer, String programmer) {
        super(properties, designer, programmer);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof AbstractDinosaurEntity dino) {
            if (!player.level().isClientSide()) {
                if (dino instanceof TamableAnimal tamable && !tamable.isOwnedBy(player)) {
                    float aggroTrait = dino.getTrait(Trait.AGGRESSIVENESS);
                    float currentAnger = dino.getFeeling(Feeling.ANGER);
                    
                    dino.setFeeling(Feeling.ANGER, currentAnger + (aggroTrait * 1.5f));
                    
                    if (aggroTrait >= 0.5f) {
                        tamable.setTarget(player);
                    }
                }

                dino.addDartDose();
                
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    ItemStack emptyDart = new ItemStack(ModItems.EMPTY_DART);
                    if (!player.getInventory().add(emptyDart)) {
                        player.drop(emptyDart, false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}