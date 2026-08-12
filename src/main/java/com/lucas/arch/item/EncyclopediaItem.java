package com.lucas.arch.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.klikli_dev.modonomicon.api.ModonomiconAPI;

public class EncyclopediaItem extends ArchItem {

    private static final Identifier BOOK_ID = Identifier.fromNamespaceAndPath("archeology_reimagined", "dinopedia");

    public EncyclopediaItem(Properties properties, String designer, String programmer) {
        super(properties, designer, programmer);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ModonomiconAPI.get().openBook(BOOK_ID, serverPlayer);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}