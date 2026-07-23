package com.lucas.arch.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.lucas.arch.registry.ModTags;
import com.lucas.arch.ArcheologyReimagined;
import com.lucas.arch.entity.ai.SeekDroppedFoodGoal;

import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;

public class AllosaurusEntity extends Animal implements GeoEntity {

    public AllosaurusEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, -1.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.FIRE , -1.0F);
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.FLOAT);

    private static final String NBT_COLOR = "AllosaurusColor";
    private static final String NBT_SCALE = "AllosaurusScale";

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(NBT_COLOR, this.entityData.get(COLOR));
        output.putFloat(NBT_SCALE, this.entityData.get(SCALE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(COLOR, input.getIntOr(NBT_COLOR, 0xFFFFFFFF));
        float visualScale = input.getFloatOr(NBT_SCALE, 1.0f);
        this.entityData.set(SCALE, visualScale);
        this.refreshDimensions();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        float variation = 0.8f + this.random.nextFloat() * 0.4f;

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHealth = this.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * variation);
            
            this.setHealth(this.getMaxHealth()); 
        }

        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double baseDamage = this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDamage * variation);
        }


        return spawnData;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 10)
            .add(Attributes.SCALE, 1.0) 
            .add(Attributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        net.minecraft.world.entity.ai.navigation.GroundPathNavigation nav =
            new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, level);
        nav.setCanFloat(true);
        nav.setMaxVisitedNodesMultiplier(this.getBbWidth() > 2.0F ? 2.0F : 1.0F);
        return nav;
    }

    public float getVisualScale() {
        return this.entityData.get(SCALE);
    }

    private static final int[] COLORS = {
        0xFFD97C3A,
        0xFF8B5A2B, 
        0xFF6B8E23
    };

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

                float visualScale = 2.7f + (this.random.nextFloat() * 0.8f);
                this.entityData.set(SCALE, visualScale);

                ArcheologyReimagined.LOGGER.info("Allosaurus scale set to {}, dimensions before refresh: {}", visualScale, this.getDimensions(this.getPose()));
                this.refreshDimensions();
                ArcheologyReimagined.LOGGER.info("Allosaurus dimensions after refresh: {}", this.getDimensions(this.getPose()));
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (SCALE.equals(dataAccessor)) {
            this.refreshDimensions();
        }
    }
    
    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float visualScale = this.entityData.get(SCALE);
        float hitboxScale = (float) Math.pow(visualScale, 0.9);
        return super.getDefaultDimensions(pose).scale(hitboxScale);
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
        controllers.add(new AnimationController<AllosaurusEntity>("main_controller", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.idle"));
        }));

        controllers.add(new AnimationController<AllosaurusEntity>("attack_controller", 0, state -> PlayState.STOP)
        .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.allosaurus.attack")));
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

    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
        
        this.goalSelector.addGoal(3, new SeekDroppedFoodGoal(this, 1.2D, 10.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.CARNIVORE_FOOD)), false));
    }
}