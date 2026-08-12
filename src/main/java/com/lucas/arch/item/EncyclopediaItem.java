package com.lucas.arch.item;

import com.klikli_dev.modonomicon.Modonomicon;
import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.client.gui.BookGuiManager;
import com.klikli_dev.modonomicon.client.gui.book.BookAddress;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.modonomicon.registry.DataComponentRegistry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EncyclopediaItem extends ArchItem {

    private final Identifier bookId;

    public EncyclopediaItem(Properties properties, String designer, String programmer, Identifier bookId) {
        super(properties, designer, programmer);
        this.bookId = bookId;
    }

    private Book getBookFor(ItemStack stack) {
        if (!stack.has((DataComponentType) DataComponentRegistry.BOOK_ID.get())) {
            stack.set((DataComponentType) DataComponentRegistry.BOOK_ID.get(), this.bookId);
        }

        Identifier res = stack.get((DataComponentType) DataComponentRegistry.BOOK_ID.get());
        return res == null ? null : BookDataManager.get().getBook(res);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        itemInHand.set((DataComponentType) DataComponentRegistry.BOOK_OPEN.get(), true);

        if (level.isClientSide()) {
            Book book = this.getBookFor(itemInHand);
            if (book != null) {
                BookGuiManager.get().openBook(BookAddress.defaultFor(book));
            } else {
                Modonomicon.LOG.error("EncyclopediaItem: ItemStack has no book tag!");
            }
        }

        return InteractionResult.SUCCESS;
    }
}