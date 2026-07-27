package com.lucas.arch.block.entity;

import com.lucas.arch.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractDinosaurEggBlockEntity<T extends Mob> extends BlockEntity {
    private int dnaQuality = 50;
    private int hatchProgress = 0;
    private int tickCounter = 0;

    public AbstractDinosaurEggBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int getMinTicksPerPercent();
    protected abstract int getMaxTicksPerPercent();
    protected abstract int getMaxHeatSources();
    protected abstract EntityType<T> getEntityType();

    public void setDnaQuality(int quality) {
        this.dnaQuality = quality;
        setChanged();
    }

    public int getHatchProgress() {
        return this.hatchProgress;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        int heatSources = countHeatSources(level, pos);
        if (heatSources <= 0) return;

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
        int clamped = Math.min(heatSources, getMaxHeatSources());
        float t = Mth.clamp((clamped - 1) / (float) (getMaxHeatSources() - 1), 0f, 1f);
        return Math.round(Mth.lerp(t, getMaxTicksPerPercent(), getMinTicksPerPercent()));
    }

    protected void hatch(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        level.removeBlock(pos, false);

        T dinosaur = getEntityType().create(serverLevel, EntitySpawnReason.BREEDING);
        if (dinosaur == null) return;

        dinosaur.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        dinosaur.setYRot(level.getRandom().nextFloat() * 360f);
        dinosaur.setXRot(0f);
        dinosaur.yRotO = dinosaur.getYRot();
        dinosaur.xRotO = dinosaur.getXRot();

        dinosaur.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), EntitySpawnReason.BREEDING, null);
        serverLevel.addFreshEntity(dinosaur);

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