package com.lucas.arch.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.TamableAnimal;
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
import com.lucas.arch.entity.ai.SeekDroppedFoodGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.EnumMap;

public class AllosaurusEntity extends TamableAnimal implements GeoEntity{ // Mudança para TamableAnimal

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // --- TRACKED DATA (Sincronização Server -> Client) ---
    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Byte> DOMINANT_STATE = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.BYTE);

    public static final EntityDataAccessor<Boolean> IS_MALE = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> AGE_TIER_SYNC = SynchedEntityData.defineId(AllosaurusEntity.class, EntityDataSerializers.STRING);

    // --- NBT KEYS ---
    private static final String NBT_COLOR = "AllosaurusColor";
    private static final String NBT_SCALE = "AllosaurusScale";
    private static final String NBT_IS_MALE = "IsMale";
    private static final String NBT_AGE_TIER = "AgeTier";
    private static final String NBT_AFFINITY = "HumanAffinity";
    private static final String NBT_GENETIC_MULTIPLIER = "GeneticStatMultiplier";
    private static final float HITBOX_SCALE_RATIO = 0.9f;
    private static final float MAX_SAFE_HITBOX_SCALE = 3.0f;

    // --- ESTRUTURAS DE DADOS ---
    private final EnumMap<Trait, Float> traits = new EnumMap<>(Trait.class);
    private final EnumMap<Feeling, Float> feelings = new EnumMap<>(Feeling.class);
    private boolean isMale;
    private AgeTier ageTier = AgeTier.BABY;
    private float baseScale = 3.1f;
    private float humanAffinity = 0.5f;
    private float geneticStatMultiplier = 1.0f;

    private static final int[] COLORS = { 0xFFD97C3A, 0xFF8B5A2B, 0xFF6B8E23 };

    public AllosaurusEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, -1.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.FIRE, -1.0F);
        
        // Inicia todos os sentimentos zerados
        for (Feeling feeling : Feeling.values()) {
            this.feelings.put(feeling, 0.0f);
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions vanillaScaledDims = super.getDefaultDimensions(pose);

        float visualScale = this.getVisualScale();
        if (visualScale <= 0f) {
            return vanillaScaledDims;
        }

        float cappedScale = Math.min(visualScale, MAX_SAFE_HITBOX_SCALE);
        float relativeRatio = (cappedScale * HITBOX_SCALE_RATIO) / visualScale;

        return vanillaScaledDims.scale(relativeRatio);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, 0xFFFFFFFF);
        builder.define(SCALE, 3.1f); 
        builder.define(DOMINANT_STATE, (byte) 0);
        builder.define(IS_MALE, true);
        builder.define(AGE_TIER_SYNC, AgeTier.BABY.name());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(NBT_COLOR, this.entityData.get(COLOR));
        output.putFloat(NBT_SCALE, this.baseScale);
        output.putBoolean(NBT_IS_MALE, this.isMale);
        output.putString(NBT_AGE_TIER, this.ageTier.name());
        output.putFloat(NBT_AFFINITY, this.humanAffinity);
        output.putFloat(NBT_GENETIC_MULTIPLIER, this.geneticStatMultiplier);

        for (Trait trait : Trait.values()) {
            output.putFloat("Trait_" + trait.name(), this.traits.getOrDefault(trait, 0.0f));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.entityData.set(COLOR, input.getIntOr(NBT_COLOR, 0xFFFFFFFF));
        this.isMale = input.getBooleanOr(NBT_IS_MALE, true);
        this.humanAffinity = input.getFloatOr(NBT_AFFINITY, 0.5f);
        this.geneticStatMultiplier = input.getFloatOr(NBT_GENETIC_MULTIPLIER, 1.0f);

        float scaleFromNbt = input.getFloatOr(NBT_SCALE, Float.NaN);
        String ageTierFromNbt = input.getStringOr(NBT_AGE_TIER, "");

        boolean hasCustomScale = !Float.isNaN(scaleFromNbt);
        boolean hasCustomAgeTier = !ageTierFromNbt.isBlank();

        if (hasCustomScale) {
            this.baseScale = scaleFromNbt;
        }
        if (hasCustomAgeTier) {
            this.ageTier = AgeTier.valueOf(ageTierFromNbt);
        }

        for (Trait trait : Trait.values()) {
            this.traits.put(trait, input.getFloatOr("Trait_" + trait.name(), 0.0f));
        }

        if (hasCustomScale || hasCustomAgeTier) {
            this.updateStats();
        } else {
            this.entityData.set(SCALE, this.getVisualScale());
        }

        this.refreshDimensions();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        this.entityData.set(IS_MALE, this.random.nextBoolean());

        if (reason == EntitySpawnReason.COMMAND || reason == EntitySpawnReason.SPAWN_ITEM_USE) {
            this.setAgeTier(AgeTier.ADULT);
            this.baseScale = 3.1f;
        } else {
            this.setAgeTier(AgeTier.BABY);
            this.baseScale = 2.7f + (this.random.nextFloat() * 0.8f);
        }
            
        // --- DISTRIBUIÇÃO GENÉTICA DE TRAITS  ---
        float totalPoints = Trait.values().length / 2.0f;
        float[] rolls = new float[Trait.values().length];
        float sum = 0;
        
        for (int i = 0; i < rolls.length; i++) {
            rolls[i] = this.random.nextFloat();
            sum += rolls[i];
        }

        int index = 0;
        for (Trait trait : Trait.values()) {
            float traitValue = Math.min((rolls[index++] / sum) * totalPoints, 1.0f);
            this.traits.put(trait, traitValue);
        }

        // Variação de Atributos de Combate
        float variation = 0.8f + this.random.nextFloat() * 0.4f;
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * variation);
            this.setHealth(this.getMaxHealth());
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * variation);
        }

        this.updateStats();
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
        net.minecraft.world.entity.ai.navigation.GroundPathNavigation nav = new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, level);
        nav.setCanFloat(true);
        nav.setMaxVisitedNodesMultiplier(this.getBbWidth() > 2.0F ? 2.0F : 1.0F);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount == 1 && !this.level().isClientSide()) {
            if (this.entityData.get(COLOR) == 0xFFFFFFFF) {
                this.entityData.set(COLOR, COLORS[this.random.nextInt(COLORS.length)]);
                this.refreshDimensions();
            }
        }
    }

    public void updateStats() {
        float ageMultiplier = this.ageTier.getScaleMultiplier();
        float effectiveScale = this.baseScale * ageMultiplier;

        if (this.getAttribute(Attributes.SCALE) != null) {
            this.getAttribute(Attributes.SCALE).setBaseValue(effectiveScale);
        }

        this.entityData.set(SCALE, effectiveScale);

        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20.0 * this.geneticStatMultiplier * ageMultiplier);
        }

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            double oldMaxHealth = this.getMaxHealth();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0 * this.geneticStatMultiplier * ageMultiplier);
            if (this.getHealth() > 0 && oldMaxHealth > 0) {
                this.setHealth((float) (this.getHealth() * (this.getMaxHealth() / oldMaxHealth)));
            }
        }
    }

    public void setFeeling(Feeling feeling, float value) {
        this.feelings.put(feeling, net.minecraft.util.Mth.clamp(value, 0.0f, 1.0f));
    }

    private Trait getAssociatedTrait(Feeling feeling) {
        return switch (feeling) {
            case ANGER -> Trait.AGGRESSIVENESS;
            case FEAR -> Trait.COWARDICE;
            case CURIOSITY -> Trait.CURIOSITY;
            case HUNGER -> Trait.GLUTTONY;
        };
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    public boolean isMale() {
        return this.entityData.get(IS_MALE);
    }

    public AgeTier getAgeTier() {
        return AgeTier.valueOf(this.entityData.get(AGE_TIER_SYNC));
    }

    public void setAgeTier(AgeTier tier) {
        if (this.getAgeTier() == tier) return;
        this.entityData.set(AGE_TIER_SYNC, tier.name());
        this.updateStats();
        this.refreshDimensions();
    }

    public float getTrait(Trait trait) { return this.traits.getOrDefault(trait, 0.0f); }
    public float getFeeling(Feeling feeling) { return this.feelings.getOrDefault(feeling, 0.0f); }
    public byte getDominantState() { return this.entityData.get(DOMINANT_STATE); }

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
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean wasHurt = super.hurtServer(level, source, amount);
        
        if (wasHurt && !this.isInvulnerableTo(level, source)) {
            float aggression = this.getTrait(Trait.AGGRESSIVENESS);
            float cowardice = this.getTrait(Trait.COWARDICE);
            
            float impact = Math.min(amount / 20.0f, 1.0f);
            
            this.setFeeling(Feeling.ANGER, this.getFeeling(Feeling.ANGER) + (impact * aggression));
            this.setFeeling(Feeling.FEAR, this.getFeeling(Feeling.FEAR) + (impact * cowardice));
        }
        
        return wasHurt;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        // Só processa lógicas orgânicas se não estiver morto ou inativo
        if (!this.isAlive()) return;

        // Loop de Decaimento: Sentimentos se acalmam conforme o tempo (baseado no Trait)
        for (Feeling feeling : Feeling.values()) {
            float currentValue = this.getFeeling(feeling);
            if (currentValue > 0.0f) {
                float traitValue = this.getTrait(getAssociatedTrait(feeling));
                
                // Fórmula: 1200 ticks * Math.max(0.2f, traitValue)
                // Ex: Trait 0.8 = 960 ticks | Trait 0.2 = 240 ticks
                int decayInterval = (int) (1200 * Math.max(0.2f, traitValue));
                
                if (decayInterval > 0 && this.tickCount % decayInterval == 0) {
                    this.setFeeling(feeling, currentValue - 0.1f);
                }
            }
        }

        // Fome Passiva: Cresce gradativamente. Dinossauros com alta Gula (GLUTTONY) sentem fome mais rápido.
        // TickCount % 1200 = A cada minuto
        if (this.tickCount % 1200 == 0) {
            float gluttony = this.getTrait(Trait.GLUTTONY);
            float hungerIncrease = 0.05f + (gluttony * 0.05f); 
            this.setFeeling(Feeling.HUNGER, this.getFeeling(Feeling.HUNGER) + hungerIncrease);
        }
    }

    public float getVisualScale() {
        if (this.getAttributes().hasAttribute(Attributes.SCALE)) {
            return (float) this.getAttributeValue(Attributes.SCALE);
        }
        
        // Fallback de segurança usando o SynchedEntityData
        return this.entityData.get(SCALE);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new SeekDroppedFoodGoal(this, 1.2D, 10.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.CARNIVORE_FOOD)), false));
    }
}