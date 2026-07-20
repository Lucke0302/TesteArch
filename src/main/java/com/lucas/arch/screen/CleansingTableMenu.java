package com.lucas.arch.screen;

import com.lucas.arch.recipe.ModCleansingRecipes;
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

public class CleansingTableMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;

    public CleansingTableMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(16), new SimpleContainerData(5));
    }

    public CleansingTableMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data) {
        super(ModMenuTypes.CLEANSING_TABLE_MENU, syncId);
        checkContainerSize(inventory, 16);
        checkContainerDataCount(data, 5);
        this.inventory = inventory;
        this.data = data;
        inventory.startOpen(playerInventory.player);

        // Grade de entrada (Fósseis) - Slots 0 a 5
        int[] inputX = {43, 62, 81};
        int[] inputY = {19, 38};
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 3; ++col) {
                int slotIndex = col + (row * 3);
                this.addSlot(new Slot(inventory, slotIndex, inputX[col], inputY[row]) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return inventory.canPlaceItem(slotIndex, stack);
                    }
                });
            }
        }

        // Grade de saída (Itens Processados) - Slots 6 a 15 (Bloqueados para colocar)
        int[] outputX = {137, 156, 174, 193, 212};
        int[] outputY = {19, 38};
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 5; ++col) {
                int slotIndex = 6 + col + (row * 5);
                this.addSlot(new Slot(inventory, slotIndex, outputX[col], outputY[row]) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false; // Bloqueado
                    }
                });
            }
        }

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

            if (invSlot < 16) {
                if (!this.moveItemStackTo(originalStack, 16, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(originalStack, newStack);
            } 
            else {
                if (ModCleansingRecipes.isValidInput(originalStack.getItem())) {
                    if (!this.moveItemStackTo(originalStack, 0, 6, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, originalStack);
        }
        return newStack;
    }

    public int getWaterLevel() { return this.data.get(0); }
    public int getProcessProgress() { return this.data.get(1); }
    public int getMaxProcessTime() { return this.data.get(2); }
    public int getFuelTime() { return this.data.get(3); }
    public int getMaxFuelTime() { return this.data.get(4); }
}