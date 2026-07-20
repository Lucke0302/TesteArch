package com.lucas.arch.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ArchItem extends Item {
    private final String designer;
    private final String programmer;

    public ArchItem(Properties properties, String designer, String programmer) {
        super(properties);
        this.designer = designer;
        this.programmer = programmer;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Designed by " + this.designer).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.accept(Component.literal("Programmed by " + this.programmer).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}