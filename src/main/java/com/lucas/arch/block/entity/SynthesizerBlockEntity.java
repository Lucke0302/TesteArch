package com.lucas.arch.block.entity; //

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import com.lucas.arch.ImplementedInventory;
import com.lucas.arch.block.SynthesizerBlock;
import com.lucas.arch.config.ModConfig;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModDataComponentTypes;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.screen.SynthesizerMenu;

public class SynthesizerBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    public int processProgress = 0;
    public int maxProcessTime = 1200; 
    public int fuelTime = 0;
    public int maxFuelTime = 0;

    protected final ContainerData data;

    public SynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SYNTHESIZER_BE, pos, state);
        this.maxProcessTime = ModConfig.get().synthesizerTicks;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> SynthesizerBlockEntity.this.processProgress;
                    case 1 -> SynthesizerBlockEntity.this.maxProcessTime;
                    case 2 -> SynthesizerBlockEntity.this.fuelTime;
                    case 3 -> SynthesizerBlockEntity.this.maxFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> SynthesizerBlockEntity.this.processProgress = value;
                    case 1 -> SynthesizerBlockEntity.this.maxProcessTime = value;
                    case 2 -> SynthesizerBlockEntity.this.fuelTime = value;
                    case 3 -> SynthesizerBlockEntity.this.maxFuelTime = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) {
            return stack.has(ModDataComponentTypes.DNA_QUALITY);
        }
        if (index == 1) {
            return stack.is(ModItems.BASIC_ORGANIC_FUEL) ||
                   stack.is(ModItems.MEDIUM_ORGANIC_FUEL) ||
                   stack.is(ModItems.ADVANCED_ORGANIC_FUEL);
        }
        return false; 
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.archeologyunnoficial.synthesizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SynthesizerMenu(syncId, playerInventory, this, this.data);
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

        boolean isCurrentlyLit = state.getValue(SynthesizerBlock.LIT);
        if (isCurrentlyLit != canProcess) {
            level.setBlock(pos, state.setValue(SynthesizerBlock.LIT, canProcess), 3);
        }

        if (canProcess) {
            this.processProgress++;
            isDirty = true;

            if (this.processProgress >= this.maxProcessTime) {
                this.processProgress = 0;
                synthesizeEmbryo(level);
                isDirty = true;
            }
        } else if (this.processProgress > 0) {
            this.processProgress = Math.max(0, this.processProgress - 2);
            isDirty = true;
        }

        if (isDirty) {
            setChanged(level, pos, state);
        }
    }

    private boolean hasValidInputs() {
        ItemStack dnaStack = this.inventory.get(0);
        ItemStack fuelStack = this.inventory.get(1);

        boolean hasDna = !dnaStack.isEmpty() && dnaStack.has(ModDataComponentTypes.DNA_QUALITY); //
        boolean hasOrganicFuel = !fuelStack.isEmpty() && (
            fuelStack.is(ModItems.BASIC_ORGANIC_FUEL) ||
            fuelStack.is(ModItems.MEDIUM_ORGANIC_FUEL) ||
            fuelStack.is(ModItems.ADVANCED_ORGANIC_FUEL)
        );

        return hasDna && hasOrganicFuel;
    }

    private boolean canInsertOutput() {
        ItemStack outputStack = this.inventory.get(2);
        return outputStack.isEmpty() || outputStack.getCount() < outputStack.getMaxStackSize();
    }

    private void synthesizeEmbryo(Level level) {
        ItemStack dnaStack = this.inventory.get(0);
        ItemStack organicFuelStack = this.inventory.get(1);

        int baseQuality = dnaStack.getOrDefault(ModDataComponentTypes.DNA_QUALITY, 50); //[cite: 1]
        int modifier = 0;

        if (organicFuelStack.is(ModItems.BASIC_ORGANIC_FUEL)) {
            modifier = -15;
        } else if (organicFuelStack.is(ModItems.ADVANCED_ORGANIC_FUEL)) {
            modifier = 15;
        }

        int finalQuality = Math.max(1, Math.min(100, baseQuality + modifier));

        float roll = level.getRandom().nextFloat() * 100f;
        boolean success = roll <= finalQuality;

        ItemStack result;
        if (success) {
            result = new ItemStack(ModItems.ALLOSAURUS_EMBRYO);
            result.set(ModDataComponentTypes.DNA_QUALITY, finalQuality); 
        } else {
            result = new ItemStack(ModItems.MEAT_CLUSTER);
        }

        dnaStack.shrink(1);
        organicFuelStack.shrink(1);

        ItemStack currentOutput = this.inventory.get(2);
        if (currentOutput.isEmpty()) {
            this.inventory.set(2, result);
        } else if (ItemStack.isSameItemSameComponents(currentOutput, result)) {
            currentOutput.grow(1);
        }
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