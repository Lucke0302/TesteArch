package com.lucas.arch.screen;

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

public class BiocatalyzerMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;

    public BiocatalyzerMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(4), new SimpleContainerData(4));
    }

    public BiocatalyzerMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data) {
        super(ModMenuTypes.BIOCATALYZER_MENU, syncId);
        checkContainerSize(inventory, 4);
        checkContainerDataCount(data, 4);
        this.inventory = inventory;
        this.data = data;
        inventory.startOpen(playerInventory.player);

        // Slots de Entrada (Cima): Slot 0 e 1
        this.addSlot(new Slot(inventory, 0, 57, 19));
        this.addSlot(new Slot(inventory, 1, 75, 19));

        // Slot de Combustível (Baixo): Slot 2
        this.addSlot(new Slot(inventory, 2, 66, 43) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.ADVANCED_ORGANIC_FUEL) || stack.is(ModItems.BIO_PROPELLANT);
            }
        });

        // Slot de Saída (Direita): Slot 3
        this.addSlot(new Slot(inventory, 3, 155, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Inventário do Jogador
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

            if (invSlot < 4) { // Do Biocatalisador para o Inventário
                if (!this.moveItemStackTo(originalStack, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(originalStack, newStack);
            } else { // Do Inventário para o Biocatalisador
                if (originalStack.is(ModItems.ADVANCED_ORGANIC_FUEL) || originalStack.is(ModItems.BIO_PROPELLANT)) {
                    if (!this.moveItemStackTo(originalStack, 2, 3, false)) return ItemStack.EMPTY;
                } else if (isInputIngredient(originalStack)) {
                    if (!this.moveItemStackTo(originalStack, 0, 2, false)) return ItemStack.EMPTY;
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

    private boolean isInputIngredient(ItemStack stack) {
        return stack.is(ModItems.FRAGMENTED_DNA) || stack.is(ModItems.EMPTY_SYRINGE) ||
               stack.is(ModItems.BITTER_BERRY_JAR) || stack.is(ModItems.EMPTY_DART);
    }

    public int getProcessProgress() { return this.data.get(0); }
    public int getMaxProcessTime() { return this.data.get(1); }
    public int getFuelTime() { return this.data.get(2); }
    public int getMaxFuelTime() { return this.data.get(3); }
}