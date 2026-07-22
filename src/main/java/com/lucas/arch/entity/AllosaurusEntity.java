package com.lucas.arch.entity;

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
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AllosaurusEntity extends Animal implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.FLOAT);


    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    private static final int[] COLORS = {
        0xFFD97C3A,
        0xFF8B5A2B, 
        0xFF6B8E23
    };

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

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("idle_controller", 0, state -> {
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