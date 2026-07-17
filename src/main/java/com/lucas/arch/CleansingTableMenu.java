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
        this(syncId, playerInventory, new SimpleContainer(17), new SimpleContainerData(5));
    }

    public CleansingTableMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data) {
        super(ModMenuTypes.CLEANSING_TABLE_MENU, syncId);
        checkContainerSize(inventory, 17);
        checkContainerDataCount(data, 5);
        this.inventory = inventory;
        this.data = data;

        inventory.startOpen(playerInventory.player);

        for (int i = 0; i < 5; ++i) {
            this.addSlot(new Slot(inventory, i, 10, 15 + (i * 18)));
        }

        this.addSlot(new Slot(inventory, 5, 50, 60));

        this.addSlot(new Slot(inventory, 6, 70, 60));

        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 5; ++col) {
                this.addSlot(new Slot(inventory, 7 + col + (row * 5), 110 + (col * 18), 20 + (row * 18)));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
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
            
            if (invSlot < 17) {
                if (!this.moveItemStackTo(originalStack, 17, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } 
            else if (!this.moveItemStackTo(originalStack, 0, 17, false)) {
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