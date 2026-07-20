package com.lucas.arch.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EncyclopediaItem extends ArchItem {

    public EncyclopediaItem(Properties properties, String designer, String programmer) {
        super(properties, designer, programmer);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aAbrindo a Enciclopédia Arqueológica..."));
        }
        return InteractionResult.SUCCESS;
    }
}