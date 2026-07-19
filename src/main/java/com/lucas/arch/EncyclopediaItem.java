package com.lucas.arch;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class EncyclopediaItem extends Item {
    public EncyclopediaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aAbrindo a Enciclopédia Arqueológica..."));
        }
        return InteractionResult.SUCCESS;
    }
}