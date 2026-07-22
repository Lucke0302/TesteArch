package com.lucas.arch.entity;

import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager; 
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.AnimationController.AnimationStateHandler;
import com.geckolib.util.GeckoLibUtil;

public class AllosaurusEntity extends Animal implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.FLOAT);

    private static final int[] COLORS = { 0xFF5A3A31, 0xFF4A553B, 0xFF7C6E55 };

    public AllosaurusEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, 0xFFFFFFFF); 
        builder.define(SCALE, 1.0f);       
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount == 1 && !this.level().isClientSide()) {
            if (this.entityData.get(COLOR) == 0xFFFFFFFF) {
                int randomColor = COLORS[this.random.nextInt(COLORS.length)];
                this.entityData.set(COLOR, randomColor);

                float randomScale = 0.75f + (this.random.nextFloat() * 0.5f);
                this.entityData.set(SCALE, randomScale);
            }
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float scale = this.entityData.get(SCALE);
        return super.getDefaultDimensions(pose).scale(scale);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }
    
    // --- GECKOLIB ANIMAÇÕES ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; 
    }
}