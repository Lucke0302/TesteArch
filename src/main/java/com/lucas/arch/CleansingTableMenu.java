package com.lucas.arch;

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

        // Grade de entrada (fósseis) - 3 colunas x 2 linhas
        int[] inputX = {39, 61, 82};
        int[] inputY = {35, 56};
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlot(new Slot(inventory, col + (row * 3), inputX[col], inputY[row]));
            }
        }

        // Grade de saída (itens processados) - 5 colunas x 2 linhas
        int[] outputX = {130, 152, 173, 195, 217};
        int[] outputY = {35, 56};
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 5; ++col) {
                this.addSlot(new Slot(inventory, 6 + col + (row * 5), outputX[col], outputY[row]));
            }
        }

        // Inventário do jogador - 9 colunas x 3 linhas
        int[] invX = {28, 49, 70, 91, 112, 133, 154, 175, 197};
        int[] invY = {101, 121, 141};
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invX[col], invY[row]));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, invX[col], 170));
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
            } 
            else if (!this.moveItemStackTo(originalStack, 0, 16, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    public int getWaterLevel() { return this.data.get(0); }
    public int getProcessProgress() { return this.data.get(1); }
    public int getMaxProcessTime() { return this.data.get(2); }
    public int getFuelTime() { return this.data.get(3); }
    public int getMaxFuelTime() { return this.data.get(4); }
}