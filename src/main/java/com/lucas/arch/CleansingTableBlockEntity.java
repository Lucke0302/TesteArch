package com.lucas.arch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
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

public class CleansingTableBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(17, ItemStack.EMPTY);

    public int waterLevel = 0;
    public int processProgress = 0;
    public int maxProcessTime = 200;
    public int fuelTime = 0;
    public int maxFuelTime = 0;

    protected final ContainerData data;

    public CleansingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CLEANSING_TABLE_BE, pos, state);

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
        return Component.translatable("block.archeologyunnoficial.cleansing_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new CleansingTableMenu(syncId, playerInventory, this, this.data); 
    }

    // --- LÓGICA DE FUNCIONAMENTO --- //

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean isDirty = false;

        if (this.fuelTime > 0) {
            this.fuelTime--;
            isDirty = true;
        }

        // Lógica de limpar o fóssil (Será preenchida no Pilar 3 com o Inventário)
        // Exemplo: if (tem agua && tem fóssil && fuelTime > 0) processProgress++;

        if (isDirty) {
            setChanged(level, pos, state);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("WaterLevel", this.waterLevel);
        output.putInt("ProcessProgress", this.processProgress);
        output.putInt("FuelTime", this.fuelTime);
        output.putInt("MaxFuelTime", this.maxFuelTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.waterLevel = input.getIntOr("WaterLevel", 0);
        this.processProgress = input.getIntOr("ProcessProgress", 0);
        this.fuelTime = input.getIntOr("FuelTime", 0);
        this.maxFuelTime = input.getIntOr("MaxFuelTime", 0);
    }
}