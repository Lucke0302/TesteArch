package com.lucas.arch.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import com.lucas.arch.ImplementedInventory;
import com.lucas.arch.config.ModConfig;
import com.lucas.arch.recipe.CleansingRecipe;
import com.lucas.arch.recipe.ModCleansingRecipes;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModDataComponentTypes;
import com.lucas.arch.registry.ModItems;
import com.lucas.arch.screen.CleansingTableMenu;

public class CleansingTableBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(16, ItemStack.EMPTY);

    public static final int MAX_WATER_LEVEL = 10;

    public int waterLevel = 0;
    public int processProgress = 0;
    public int maxProcessTime = 200;
    public int fuelTime = 0;
    public int maxFuelTime = 0;

    protected final ContainerData data;

    public CleansingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CLEANSING_TABLE_BE, pos, state);
        this.maxProcessTime = ModConfig.get().cleansingTableTicks;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CleansingTableBlockEntity.this.waterLevel;
                    case 1 -> CleansingTableBlockEntity.this.processProgress;
                    case 2 -> CleansingTableBlockEntity.this.maxProcessTime;
                    case 3 -> CleansingTableBlockEntity.this.fuelTime;
                    case 4 -> CleansingTableBlockEntity.this.maxFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CleansingTableBlockEntity.this.waterLevel = value;
                    case 1 -> CleansingTableBlockEntity.this.processProgress = value;
                    case 2 -> CleansingTableBlockEntity.this.maxProcessTime = value;
                    case 3 -> CleansingTableBlockEntity.this.fuelTime = value;
                    case 4 -> CleansingTableBlockEntity.this.maxFuelTime = value;
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.archeology_reimagined.cleansing_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new CleansingTableMenu(syncId, playerInventory, this, this.data); 
    }

    // --- LÓGICA DE FUNCIONAMENTO --- //

    /**
     * Tenta encher o tanque de água com um balde de água segurado pelo jogador.
     * @return true se consumiu o balde (tanque tinha espaço), false se já estava cheio.
     */
    public boolean tryAddWater() {
        if (this.waterLevel >= MAX_WATER_LEVEL) {
            return false;
        }
        this.waterLevel = Math.min(MAX_WATER_LEVEL, this.waterLevel + 10);
        return true;
    }

    /**
     * Tenta adicionar combustível a partir de um item que a fornalha vanilla aceitaria.
     * @return true se o item era um combustível válido e foi consumido, false caso contrário.
     */
    public boolean tryAddFuel(Level level, ItemStack fuelStack) {
        if (this.fuelTime > 0) {
            // Só aceita combustível novo quando o atual acabar
            return false;
        }

        int burnDuration = level.fuelValues().burnDuration(fuelStack);
        if (burnDuration <= 0) {
            return false;
        }

        this.fuelTime = burnDuration;
        this.maxFuelTime = burnDuration;
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index >= 0 && index <= 5) {
            return ModCleansingRecipes.isValidInput(stack.getItem());
        }
        return false; 
    }

    private boolean canInsert(ItemStack result) {
        int remaining = result.getCount();
        for (int i = 6; i <= 15; i++) {
            ItemStack slotStack = this.inventory.get(i);
            if (slotStack.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(slotStack, result)) {
                remaining -= (slotStack.getMaxStackSize() - slotStack.getCount());
                if (remaining <= 0) return true;
            }
        }
        return false;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean isDirty = false;
        if (this.fuelTime > 0) {
            this.fuelTime--;
            isDirty = true;
        }

        int inputSlot = findValidInputSlot();
        
        boolean canProcess = inputSlot != -1 && this.waterLevel >= ModConfig.get().cleansingTableWaterCost && this.fuelTime > 0;

        if (canProcess) {
            this.processProgress++;
            isDirty = true;

            if (this.processProgress >= this.maxProcessTime) {
                this.processProgress = 0;
                if (processFossil(level, inputSlot)) {
                    isDirty = true;
                }
            }
        } else if (this.processProgress > 0) {
            this.processProgress = Math.max(0, this.processProgress - 2);
            isDirty = true;
        }

        if (isDirty) {
            setChanged(level, pos, state);
        }
    }

    private int findValidInputSlot() {
        for (int i = 0; i <= 5; i++) {
            ItemStack stack = this.inventory.get(i);
            if (!stack.isEmpty() && ModCleansingRecipes.isValidInput(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private boolean processFossil(Level level, int inputSlot) {
        ItemStack inputStack = this.inventory.get(inputSlot);

        if (inputStack.is(ModItems.MOSQUITO_IN_AMBER)) {
            ItemStack amber = new ItemStack(ModItems.AMBER);
            
            Item dnaType = level.getRandom().nextBoolean() ? ModItems.DEFAULT_MAMMAL_DNA : ModItems.DEFAULT_REPTILE_DNA;
            ItemStack dna = new ItemStack(dnaType);
            
            dna.set(ModDataComponentTypes.DNA_QUALITY, 85 + level.getRandom().nextInt(16));

            if (!canInsert(amber) || !canInsert(dna)) return false; 

            insertIntoOutput(amber);
            insertIntoOutput(dna);
            inputStack.shrink(1);
            this.waterLevel = Math.max(0, this.waterLevel - ModConfig.get().cleansingTableWaterCost);
            return true;
        }

        CleansingRecipe recipe = ModCleansingRecipes.get(inputStack.getItem());
        if (recipe == null) return false;

        boolean success = level.getRandom().nextFloat() < ModCleansingRecipes.SUCCESS_CHANCE;
        Item resultItem;

        if (success) {
            if (level.getRandom().nextFloat() < 0.20f) {
                resultItem = ModItems.FRAGMENTED_DNA;
            } else {
                resultItem = recipe.successOutput();
            }
        } else {
            resultItem = ModCleansingRecipes.rollFailureItem(recipe, level.getRandom());
        }

        if (resultItem == null) return false;
        ItemStack resultStack = new ItemStack(resultItem);

        if (success && resultItem != ModItems.FRAGMENTED_DNA) {
            int quality = 40 + level.getRandom().nextInt(46);
            resultStack.set(ModDataComponentTypes.DNA_QUALITY, quality);
        }

        if (!canInsert(resultStack)) return false;

        insertIntoOutput(resultStack);
        inputStack.shrink(1);
        this.waterLevel -= ModConfig.get().cleansingTableWaterCost;
        return true;
    }

    private boolean insertIntoOutput(ItemStack result) {
        for (int i = 6; i <= 15; i++) {
            ItemStack slotStack = this.inventory.get(i);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, result)
                && slotStack.getCount() < slotStack.getMaxStackSize()) {
                slotStack.grow(result.getCount());
                return true;
            }
        }
        for (int i = 6; i <= 15; i++) {
            if (this.inventory.get(i).isEmpty()) {
                this.inventory.set(i, result);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("WaterLevel", this.waterLevel);
        output.putInt("ProcessProgress", this.processProgress);
        output.putInt("FuelTime", this.fuelTime);
        output.putInt("MaxFuelTime", this.maxFuelTime);
        ContainerHelper.saveAllItems(output, this.inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.waterLevel = input.getIntOr("WaterLevel", 0);
        this.processProgress = input.getIntOr("ProcessProgress", 0);
        this.fuelTime = input.getIntOr("FuelTime", 0);
        this.maxFuelTime = input.getIntOr("MaxFuelTime", 0);
        this.inventory.clear();
        ContainerHelper.loadAllItems(input, this.inventory);
    }
}