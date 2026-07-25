package com.lucas.arch.block.entity;

import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.registry.ModBlockEntities;
import com.lucas.arch.registry.ModEntities;
import com.lucas.arch.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AllosaurusEggBlockEntity extends BlockEntity {

    private static final int MIN_TICKS_PER_PERCENT = 50;  
    private static final int MAX_TICKS_PER_PERCENT = 200; 
    private static final int HEAT_SOURCES_FOR_MAX_RATE = 5;

    private int dnaQuality = 50;
    private int hatchProgress = 0; 
    private int tickCounter = 0;

    public AllosaurusEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOSAURUS_EGG_BE, pos, state);
    }

    public void setDnaQuality(int quality) {
        this.dnaQuality = quality;
        setChanged();
    }

    public int getHatchProgress() {
        return this.hatchProgress;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        int heatSources = countHeatSources(level, pos);
        if (heatSources <= 0) {
            return;
        }

        this.tickCounter++;
        int ticksNeeded = computeTicksPerPercent(heatSources);

        if (this.tickCounter >= ticksNeeded) {
            this.tickCounter = 0;
            this.hatchProgress++;
            setChanged(level, pos, state);

            if (this.hatchProgress >= 100) {
                hatch(level, pos);
            }
        }
    }

    private int countHeatSources(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(ModTags.Blocks.EGG_HEAT_SOURCES)) {
                count++;
            }
        }
        return count;
    }

    private int computeTicksPerPercent(int heatSources) {
        int clamped = Math.min(heatSources, HEAT_SOURCES_FOR_MAX_RATE);
        float t = Mth.clamp((clamped - 1) / (float) (HEAT_SOURCES_FOR_MAX_RATE - 1), 0f, 1f);
        return Math.round(Mth.lerp(t, MAX_TICKS_PER_PERCENT, MIN_TICKS_PER_PERCENT));
    }

    private void hatch(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        level.removeBlock(pos, false);

        AllosaurusEntity allosaurus = ModEntities.ALLOSAURUS.create(serverLevel, EntitySpawnReason.BREEDING);
        if (allosaurus == null) return;

        allosaurus.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        allosaurus.setYRot(level.getRandom().nextFloat() * 360f);
        allosaurus.setXRot(0f);
        allosaurus.yRotO = allosaurus.getYRot();
        allosaurus.xRotO = allosaurus.getXRot();

        allosaurus.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), EntitySpawnReason.BREEDING, null);

        serverLevel.addFreshEntity(allosaurus);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
            pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("DnaQuality", this.dnaQuality);
        output.putInt("HatchProgress", this.hatchProgress);
        output.putInt("TickCounter", this.tickCounter);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.dnaQuality = input.getIntOr("DnaQuality", 50);
        this.hatchProgress = input.getIntOr("HatchProgress", 0);
        this.tickCounter = input.getIntOr("TickCounter", 0);
    }
}