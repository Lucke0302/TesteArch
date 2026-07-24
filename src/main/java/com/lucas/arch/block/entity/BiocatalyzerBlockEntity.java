package com.lucas.arch.block.entity;

import com.lucas.arch.ImplementedInventory;
import com.lucas.arch.block.BiocatalyzerBlock;
import com.lucas.arch.config.ModConfig;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.screen.BiocatalyzerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
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

public class BiocatalyzerBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    public int processProgress = 0;
    public int maxProcessTime = 400;
    public int fuelTime = 0;
    public int maxFuelTime = 0;

    protected final ContainerData data;

    public BiocatalyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIOCATALYZER_BE, pos, state);
        this.maxProcessTime = ModConfig.get().biocatalyzerTicks;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> BiocatalyzerBlockEntity.this.processProgress;
                    case 1 -> BiocatalyzerBlockEntity.this.maxProcessTime;
                    case 2 -> BiocatalyzerBlockEntity.this.fuelTime;
                    case 3 -> BiocatalyzerBlockEntity.this.maxFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> BiocatalyzerBlockEntity.this.processProgress = value;
                    case 1 -> BiocatalyzerBlockEntity.this.maxProcessTime = value;
                    case 2 -> BiocatalyzerBlockEntity.this.fuelTime = value;
                    case 3 -> BiocatalyzerBlockEntity.this.maxFuelTime = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.archeology_reimagined.biocatalyzer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new BiocatalyzerMenu(syncId, playerInventory, this, this.data);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0 || index == 1) {
            return stack.is(ModItems.FRAGMENTED_DNA) || stack.is(ModItems.EMPTY_SYRINGE) ||
                   stack.is(ModItems.BITTER_BERRY_JAR) || stack.is(ModItems.EMPTY_DART);
        }
        if (index == 2) {
            return stack.is(ModItems.ADVANCED_ORGANIC_FUEL) || stack.is(ModItems.BIO_PROPELLANT);
        }
        return false;
    }

    public enum RecipeType {
        NONE,
        ADDITIVE_SYRINGE,
        TRANQUILIZER_DART
    }

    private RecipeType getActiveRecipeType() {
        ItemStack in1 = this.inventory.get(0);
        ItemStack in2 = this.inventory.get(1);

        boolean hasSyringeInputs = (in1.is(ModItems.FRAGMENTED_DNA) && in2.is(ModItems.EMPTY_SYRINGE)) ||
                                   (in2.is(ModItems.FRAGMENTED_DNA) && in1.is(ModItems.EMPTY_SYRINGE));
        if (hasSyringeInputs) {
            return RecipeType.ADDITIVE_SYRINGE;
        }

        boolean hasDartInputs = (in1.is(ModItems.BITTER_BERRY_JAR) && in2.is(ModItems.EMPTY_DART)) ||
                                (in2.is(ModItems.BITTER_BERRY_JAR) && in1.is(ModItems.EMPTY_DART));
        if (hasDartInputs) {
            return RecipeType.TRANQUILIZER_DART;
        }

        return RecipeType.NONE;
    }

    private boolean isFuelValidForRecipe(RecipeType recipe, ItemStack fuelStack) {
        if (recipe == RecipeType.ADDITIVE_SYRINGE) {
            return fuelStack.is(ModItems.ADVANCED_ORGANIC_FUEL);
        }
        if (recipe == RecipeType.TRANQUILIZER_DART) {
            return fuelStack.is(ModItems.BIO_PROPELLANT);
        }
        return false;
    }

    private boolean canOutput(RecipeType recipe) {
        ItemStack out = this.inventory.get(3);
        if (out.isEmpty()) return true;
        ItemStack expected = recipe == RecipeType.ADDITIVE_SYRINGE ? new ItemStack(ModItems.FULL_SYRINGE) : new ItemStack(ModItems.FULL_DART);
        return out.is(expected.getItem()) && out.getCount() < out.getMaxStackSize();
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean isDirty = false;
        RecipeType recipe = getActiveRecipeType();

        if (this.fuelTime > 0) {
            this.fuelTime--;
            isDirty = true;
        }

        if (this.fuelTime <= 0 && recipe != RecipeType.NONE && canOutput(recipe)) {
            ItemStack fuelStack = this.inventory.get(2);
            if (isFuelValidForRecipe(recipe, fuelStack)) {
                fuelStack.shrink(1);
                this.fuelTime = ModConfig.get().biocatalyzerTicks;
                this.maxFuelTime = this.fuelTime;
                isDirty = true;
            }
        }

        boolean canProcess = recipe != RecipeType.NONE && this.fuelTime > 0 && canOutput(recipe);
        boolean isCurrentlyLit = state.getValue(BiocatalyzerBlock.LIT);

        if (isCurrentlyLit != canProcess) {
            level.setBlock(pos, state.setValue(BiocatalyzerBlock.LIT, canProcess), 3);
        }

        if (canProcess) {
            this.processProgress++;
            isDirty = true;
            if (this.processProgress >= this.maxProcessTime) {
                this.processProgress = 0;
                craftItem(recipe, level, pos);
                isDirty = true;
            }
        } else if (this.processProgress > 0) {
            this.processProgress = Math.max(0, this.processProgress - 2);
            isDirty = true;
        }

        if (isDirty) setChanged(level, pos, state);
    }

    private void craftItem(RecipeType recipe, Level level, BlockPos pos) {
        if (recipe == RecipeType.ADDITIVE_SYRINGE) {
            this.inventory.get(0).shrink(1);
            this.inventory.get(1).shrink(1);

            ItemStack currentOut = this.inventory.get(3);
            if (currentOut.isEmpty()) {
                this.inventory.set(3, new ItemStack(ModItems.FULL_SYRINGE));
            } else {
                currentOut.grow(1);
            }
        } else if (recipe == RecipeType.TRANQUILIZER_DART) {
            int jarSlot = this.inventory.get(0).is(ModItems.BITTER_BERRY_JAR) ? 0 : 1;
            int dartSlot = jarSlot == 0 ? 1 : 0;

            this.inventory.get(dartSlot).shrink(1);

            ItemStack jarStack = this.inventory.get(jarSlot);
            if (jarStack.getCount() == 1) {
                // Substitui diretamente pelo frasco de vidro no slot
                this.inventory.set(jarSlot, new ItemStack(Items.GLASS_BOTTLE));
            } else {
                jarStack.shrink(1);
                // Se o frasco de bagas estivesse em um pack maior que 1, dropa o frasco no mundo
                if (level != null && !level.isClientSide()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(Items.GLASS_BOTTLE));
                }
            }

            ItemStack currentOut = this.inventory.get(3);
            if (currentOut.isEmpty()) {
                this.inventory.set(3, new ItemStack(ModItems.FULL_DART));
            } else {
                currentOut.grow(1);
            }
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