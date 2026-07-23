package com.lucas.arch.block.entity;

import com.lucas.arch.ImplementedInventory;
import com.lucas.arch.block.FuserBlock;
import com.lucas.arch.config.ModConfig;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModDataComponentTypes;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.screen.FuserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class FuserBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
    public int processProgress = 0;
    public int maxProcessTime = 2400; 
    public int fuelTime = 0;
    public int maxFuelTime = 0;

    protected final ContainerData data;

    public FuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSER_BE, pos, state);
        this.maxProcessTime = ModConfig.get().fuserTicks;
        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> FuserBlockEntity.this.processProgress;
                    case 1 -> FuserBlockEntity.this.maxProcessTime;
                    case 2 -> FuserBlockEntity.this.fuelTime;
                    case 3 -> FuserBlockEntity.this.maxFuelTime;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                switch (index) {
                    case 0 -> FuserBlockEntity.this.processProgress = value;
                    case 1 -> FuserBlockEntity.this.maxProcessTime = value;
                    case 2 -> FuserBlockEntity.this.fuelTime = value;
                    case 3 -> FuserBlockEntity.this.maxFuelTime = value;
                }
            }
            @Override public int getCount() { return 4; }
        };
    }

    @Override
    public NonNullList<ItemStack> getItems() { return this.inventory; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.archeology_reimagined.fuser");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FuserMenu(syncId, playerInventory, this, this.data);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) return stack.is(ModItems.ALLOSAURUS_EMBRYO) && stack.has(ModDataComponentTypes.DNA_QUALITY);
        if (index == 1) return stack.is(Items.EGG) || stack.is(Items.TURTLE_EGG) || stack.is(Items.SNIFFER_EGG);
        return false;
    }

    public boolean tryAddFuel(Level level, ItemStack fuelStack) {
        if (this.fuelTime > 0) return false;
        int burnDuration = level.fuelValues().burnDuration(fuelStack);
        if (burnDuration <= 0) return false;
        this.fuelTime = burnDuration;
        this.maxFuelTime = burnDuration;
        return true;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean isDirty = false;

        if (this.fuelTime > 0) {
            this.fuelTime--;
            isDirty = true;
        }

        boolean canProcess = hasValidInputs() && this.fuelTime > 0 && canInsertOutput();
        boolean isCurrentlyLit = state.getValue(FuserBlock.LIT);

        if (isCurrentlyLit != canProcess) {
            level.setBlock(pos, state.setValue(FuserBlock.LIT, canProcess), 3);
        }

        if (canProcess) {
            this.processProgress++;
            isDirty = true;
            if (this.processProgress >= this.maxProcessTime) {
                this.processProgress = 0;
                fuseEgg(level);
                isDirty = true;
            }
        } else if (this.processProgress > 0) {
            this.processProgress = Math.max(0, this.processProgress - 2);
            isDirty = true;
        }

        if (isDirty) setChanged(level, pos, state);
    }

    private boolean hasValidInputs() {
        ItemStack embryo = this.inventory.get(0);
        ItemStack egg = this.inventory.get(1);
        boolean hasEmbryo = !embryo.isEmpty() && embryo.is(ModItems.ALLOSAURUS_EMBRYO) && embryo.has(ModDataComponentTypes.DNA_QUALITY);
        boolean hasEgg = !egg.isEmpty() && (egg.is(Items.EGG) || egg.is(Items.TURTLE_EGG) || egg.is(Items.SNIFFER_EGG));
        return hasEmbryo && hasEgg;
    }

    private boolean canInsertOutput() {
        return this.inventory.get(2).isEmpty();
    }

    private void fuseEgg(Level level) {
        ItemStack embryoStack = this.inventory.get(0);
        ItemStack eggStack = this.inventory.get(1);

        int baseQuality = embryoStack.getOrDefault(ModDataComponentTypes.DNA_QUALITY, 50);
        int bonus = 0;

        if (eggStack.is(Items.TURTLE_EGG)) {
            bonus = 15;
        } else if (eggStack.is(Items.SNIFFER_EGG)) {
            bonus = 30;
        }

        int finalQuality = Math.max(1, Math.min(100, baseQuality + bonus));
        
        float roll = level.getRandom().nextFloat() * 100f;
        boolean success = roll <= finalQuality;

        ItemStack result;
        if (success) {
            result = new ItemStack(ModItems.ALLOSAURUS_EGG);
            result.set(ModDataComponentTypes.DNA_QUALITY, finalQuality);
        } else {
            int amount = 3 + level.getRandom().nextInt(4);
            result = new ItemStack(ModItems.MEAT_CLUSTER, amount);
        }

        embryoStack.shrink(1);
        eggStack.shrink(1);
        this.inventory.set(2, result);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ProcessProgress", this.processProgress);
        output.putInt("FuelTime", this.fuelTime);
        output.putInt("MaxFuelTime", this.maxFuelTime);
        ContainerHelper.saveAllItems(output, this.inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.processProgress = input.getIntOr("ProcessProgress", 0);
        this.fuelTime = input.getIntOr("FuelTime", 0);
        this.maxFuelTime = input.getIntOr("MaxFuelTime", 0);
        this.inventory.clear();
        ContainerHelper.loadAllItems(input, this.inventory);
    }
}