package com.lucas.arch.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class ArchItemNameBlockItem extends BlockItem {
    private final String designer;
    private final String programmer;

    public ArchItemNameBlockItem(Block block, Properties properties, String designer, String programmer) {
        super(block, properties);
        this.designer = designer;
        this.programmer = programmer;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Designed by " + this.designer).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.accept(Component.literal("Programmed by " + this.programmer).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}