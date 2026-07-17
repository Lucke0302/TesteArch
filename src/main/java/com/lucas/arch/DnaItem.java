package com.lucas.arch;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class DnaItem extends Item {
    
    public DnaItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        
        if (stack.has(ModDataComponentTypes.DNA_QUALITY)) {
            int quality = stack.get(ModDataComponentTypes.DNA_QUALITY);
            
            ChatFormatting qualityColor;
            if (quality < 55) {
                qualityColor = ChatFormatting.RED;
            } else if (quality < 70) {
                qualityColor = ChatFormatting.YELLOW;
            } else if (quality < 85) {
                qualityColor = ChatFormatting.GREEN;
            } else {
                qualityColor = ChatFormatting.AQUA;
            }
            
            MutableComponent colorfulValue = Component.literal(quality + "%").withStyle(qualityColor);
            
            tooltip.accept(Component.translatable("tooltip.archeologyunnoficial.dna_quality", colorfulValue));
        }
        
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}