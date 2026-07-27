package com.lucas.arch.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import com.lucas.arch.registry.ModDataComponentTypes;

import java.util.function.Consumer;

public class BloodSyringeItem extends ArchItem {
    public BloodSyringeItem(Item.Properties properties, String designer, String programmer) {
        super(properties.stacksTo(1), designer, programmer); 
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        
        Identifier species = stack.get(ModDataComponentTypes.SYRINGE_SPECIES);
        if (species != null) {
            tooltip.accept(Component.translatable("tooltip.archeology_reimagined.species")
                .append(Component.literal(": " + species.getPath()))
                .withStyle(ChatFormatting.RED));
        }
    }
}