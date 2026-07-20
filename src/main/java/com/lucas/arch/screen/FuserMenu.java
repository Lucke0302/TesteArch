package com.lucas.arch.screen;

import com.lucas.arch.registry.ModDataComponentTypes;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FuserMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;

    public FuserMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    public FuserMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data) {
        super(ModMenuTypes.FUSER_MENU, syncId);
        checkContainerSize(inventory, 3);
        checkContainerDataCount(data, 4);

        this.inventory = inventory;
        this.data = data;
        inventory.startOpen(playerInventory.player);

        // Slot 0: Embrião
        this.addSlot(new Slot(inventory, 0, 66, 19) {
            @Override public boolean mayPlace(ItemStack stack) { return inventory.canPlaceItem(0, stack); }
        });

        // Slot 1: Ovos Vanilla
        this.addSlot(new Slot(inventory, 1, 66, 43) {
            @Override public boolean mayPlace(ItemStack stack) { return inventory.canPlaceItem(1, stack); }
        });

        // Slot 2: Resultado
        this.addSlot(new Slot(inventory, 2, 155, 29) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Inventário
        int[] invX = {45, 63, 81, 99, 117, 135, 153, 171, 189};
        int[] invY = {75, 93, 111};
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invX[col], invY[row]));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, invX[col], 134));
        }

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < 3) {
                if (!this.moveItemStackTo(originalStack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(originalStack, newStack);
            } else {
                // Shift-Click: Embriões vão pro Slot 0
                if (originalStack.is(ModItems.ALLOSAURUS_EMBRYO) && originalStack.has(ModDataComponentTypes.DNA_QUALITY)) {
                    if (!this.moveItemStackTo(originalStack, 0, 1, false)) return ItemStack.EMPTY;
                } 
                // Shift-Click: Ovos Vanilla vão pro Slot 1
                else if (originalStack.is(Items.EGG) || originalStack.is(Items.TURTLE_EGG) || originalStack.is(Items.SNIFFER_EGG)) {
                    if (!this.moveItemStackTo(originalStack, 1, 2, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (originalStack.getCount() == newStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, originalStack);
        }
        return newStack;
    }

    public int getProcessProgress() { return this.data.get(0); }
    public int getMaxProcessTime() { return this.data.get(1); }
    public int getFuelTime() { return this.data.get(2); }
    public int getMaxFuelTime() { return this.data.get(3); }
}