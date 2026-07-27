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

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import com.lucas.arch.entity.ai.DinosaurFollowOwnerGoal;
import com.lucas.arch.entity.ai.DinosaurTemptGoal;
import com.lucas.arch.entity.ai.FearBehaviorGoal;
import com.lucas.arch.entity.ai.AngerBehaviorGoal;
import com.lucas.arch.entity.ai.CuriosityBehaviorGoal;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;

import java.util.EnumMap;

/**
 * Herbívoro. Reaproveita toda a arquitetura de Feelings/Traits/BehaviorResolver do Allosaurus,
 * mas: (1) não caça — HungerBehaviorGoal deve resolver para SEEK_GROUND_FOOD/BEG_OWNER usando
 * ModTags.Items.HERBIVORE_FOOD ao invés de CARNIVORE_FOOD; (2) recebe o dobro de saturação em
 * toda alimentação (compensando a ausência do bônus de caça) e aplica o mesmo multiplicador
 * no decréscimo de fome; (3) pastar em grama (graze()) é chamado por uma Goal dedicada (Parte 2)
 * que transforma grass_block em dirt e concede saturação.
 */
public class PachycephalosaurusEntity extends TamableAnimal implements GeoEntity, FeelingDrivenEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // --- TRACKED DATA (Sincronização Server -> Client) ---
    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PachycephalosaurusEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(PachycephalosaurusEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Byte> DOMINANT_STATE = SynchedEntityData.defineId(PachycephalosaurusEntity.class, EntityDataSerializers.BYTE);

    public static final EntityDataAccessor<Boolean> IS_MALE = SynchedEntityData.defineId(PachycephalosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Byte> AGE_TIER_SYNC = SynchedEntityData.defineId(PachycephalosaurusEntity.class, EntityDataSerializers.BYTE);

    // --- NBT KEYS ---
    private static final String NBT_COLOR = "PachyColor";
    private static final String NBT_SCALE = "PachyScale";
    private static final String NBT_IS_MALE = "IsMale";
    private static final String NBT_AGE_TIER = "AgeTier";
    private static final String NBT_AFFINITY = "HumanAffinity";
    private static final String NBT_GENETIC_MULTIPLIER = "GeneticStatMultiplier";
    private static final float HITBOX_SCALE_RATIO = 1.5f;
    private static final float MAX_SAFE_HITBOX_SCALE = 3.0f;
    private static final float DOMINANT_STATE_THRESHOLD = 0.3f;

    /** Multiplicador aplicado à saturação ganha (e ao decréscimo de fome correspondente),
     *  já que o Pachy não tem o bônus de caça que o Allosaurus recebe. */
    public static final float HERBIVORE_SATURATION_MULTIPLIER = 2.0f;

    // --- ESTRUTURAS DE DADOS ---
    private final EnumMap<Trait, Float> traits = new EnumMap<>(Trait.class);
    private final EnumMap<Feeling, Float> feelings = new EnumMap<>(Feeling.class);
    private AgeTier ageTier = AgeTier.BABY;
    private float baseScale = 1f;
    private float humanAffinity = 0.5f;
    private float geneticStatMultiplier = 1.0f;
    private int growthTicks = 0;
    private float accumulatedSaturation = 0.0f;

    private static final int TICKS_TO_GROW = 1200;
    private static final float SATURATION_TO_GROW = 4.0f;

    private static final int[] COLORS = { 0xFF8B7355, 0xFF6B6B4A, 0xFFA0826D };

    public PachycephalosaurusEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, -1.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.FIRE, -1.0F);

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
        builder.define(SCALE, 1f); 
        builder.define(DOMINANT_STATE, (byte) 0);
        builder.define(IS_MALE, true);
        builder.define(AGE_TIER_SYNC, (byte) AgeTier.BABY.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(NBT_COLOR, this.entityData.get(COLOR));
        output.putFloat(NBT_SCALE, this.baseScale);
        output.putBoolean(NBT_IS_MALE, this.entityData.get(IS_MALE));
        output.putString(NBT_AGE_TIER, this.ageTier.name());
        output.putFloat(NBT_AFFINITY, this.humanAffinity);
        output.putFloat(NBT_GENETIC_MULTIPLIER, this.geneticStatMultiplier);
        output.putInt("GrowthTicks", this.growthTicks);
        output.putFloat("AccumulatedSaturation", this.accumulatedSaturation);
        for (Trait trait : Trait.values()) {
            output.putFloat("Trait_" + trait.name(), this.traits.getOrDefault(trait, 0.0f));
        }
        for (Feeling feeling : Feeling.values()) {
            output.putFloat("Feeling_" + feeling.name(), this.feelings.getOrDefault(feeling, 0.0f));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.entityData.set(COLOR, input.getIntOr(NBT_COLOR, 0xFFFFFFFF));
        this.entityData.set(IS_MALE, input.getBooleanOr(NBT_IS_MALE, true));
        this.humanAffinity = input.getFloatOr(NBT_AFFINITY, 0.5f);
        this.geneticStatMultiplier = input.getFloatOr(NBT_GENETIC_MULTIPLIER, 1.0f);

        float scaleFromNbt = input.getFloatOr(NBT_SCALE, Float.NaN);
        String ageTierFromNbt = input.getStringOr(NBT_AGE_TIER, "");

        boolean hasCustomScale = !Float.isNaN(scaleFromNbt);
        boolean hasCustomAgeTier = !ageTierFromNbt.isBlank();

        this.growthTicks = input.getIntOr("GrowthTicks", 0);
        this.accumulatedSaturation = input.getFloatOr("AccumulatedSaturation", 0.0f);

        if (hasCustomScale) {
            this.baseScale = scaleFromNbt;
        }
        if (hasCustomAgeTier) {
            this.ageTier = AgeTier.valueOf(ageTierFromNbt);
            this.entityData.set(AGE_TIER_SYNC, (byte) this.ageTier.ordinal());
        }

        for (Trait trait : Trait.values()) {
            this.traits.put(trait, input.getFloatOr("Trait_" + trait.name(), 0.0f));
        }
        for (Feeling feeling : Feeling.values()) {
            this.feelings.put(feeling, input.getFloatOr("Feeling_" + feeling.name(), 0.0f));
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

        this.baseScale = 1f + (this.random.nextFloat() * 0.3f);

        if (reason == EntitySpawnReason.COMMAND || reason == EntitySpawnReason.SPAWN_ITEM_USE) {
            this.setAgeTier(AgeTier.ADULT);
        } else {
            this.setAgeTier(AgeTier.BABY);
        }

        // --- DISTRIBUIÇÃO GENÉTICA DE TRAITS ---
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

        // Variação de Atributos de Combate (defensivo — cabeçada, não caça)
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
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.ATTACK_DAMAGE, 6)
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
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0 * this.geneticStatMultiplier * ageMultiplier);
        }

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            double oldMaxHealth = this.getMaxHealth();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0 * this.geneticStatMultiplier * ageMultiplier);
            if (this.getHealth() > 0 && oldMaxHealth > 0) {
                this.setHealth((float) (this.getHealth() * (this.getMaxHealth() / oldMaxHealth)));
            }
        }
    }

    private void updateDominantState() {
        Feeling dominant = null;
        float highestEffectiveValue = DOMINANT_STATE_THRESHOLD;

        float aggro = this.getTrait(Trait.AGGRESSIVENESS);
        float coward = this.getTrait(Trait.COWARDICE);
        float glut = this.getTrait(Trait.GLUTTONY);
        float curio = this.getTrait(Trait.CURIOSITY);

        for (Feeling feeling : Feeling.values()) {
            float rawValue = this.getFeeling(feeling);
            float multiplier = 1.0f;

            switch (feeling) {
                case ANGER -> multiplier += (aggro * 0.6f) + (curio * aggro * 0.4f) - (coward * 0.5f);
                case FEAR -> multiplier += (coward * 0.6f) - (aggro * 0.5f) - (curio * 0.3f);
                case HUNGER -> multiplier += (glut * 0.5f) + (aggro * 0.3f);
                case CURIOSITY -> multiplier += (curio * 0.5f) + (glut * curio * 0.3f) - (coward * 0.6f);
            }

            float effectiveValue = rawValue * Math.max(0.1f, multiplier);

            if (effectiveValue > highestEffectiveValue) {
                highestEffectiveValue = effectiveValue;
                dominant = feeling;
            }
        }

        byte newState = (byte) (dominant == null ? 0 : dominant.ordinal() + 1);
        if (this.entityData.get(DOMINANT_STATE) != newState) {
            this.entityData.set(DOMINANT_STATE, newState);
        }
    }

    /**
     * Alimenta o Pachy com um item do tag HERBIVORE_FOOD ou com o resultado de graze().
     * Diferente do Allosaurus, não existe "isHuntBonus": todo consumo já recebe o
     * multiplicador HERBIVORE_SATURATION_MULTIPLIER, pois o herbívoro não tem bônus de caça.
     */
    public void feedSaturation(ItemStack foodStack) {
        if (foodStack.has(DataComponents.FOOD)) {
            FoodProperties food = foodStack.get(DataComponents.FOOD);
            float value = food.nutrition() * HERBIVORE_SATURATION_MULTIPLIER;

            this.accumulatedSaturation += value;

            float currentHunger = this.getFeeling(Feeling.HUNGER);
            this.setFeeling(Feeling.HUNGER, Math.max(0, currentHunger - (value / 50.0f)));
        }
    }

    /**
     * Ganho de saturação "genérico" pra pastagem (grama -> terra), quando não há um
     * ItemStack real de comida envolvido. Usado pela goal de pastar (Parte 2).
     */
    public void grazeSaturation(float baseNutrition) {
        float value = baseNutrition * HERBIVORE_SATURATION_MULTIPLIER;
        this.accumulatedSaturation += value;

        float currentHunger = this.getFeeling(Feeling.HUNGER);
        this.setFeeling(Feeling.HUNGER, Math.max(0, currentHunger - (value / 50.0f)));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isTame() && !itemStack.is(ModTags.Items.HERBIVORE_FOOD)) {
            return super.mobInteract(player, hand);
        }

        if (itemStack.is(ModTags.Items.HERBIVORE_FOOD)) {
            if (!this.level().isClientSide()) {
                this.feedSaturation(itemStack);

                if (!this.isTame()) {
                    AgeTier age = this.getAgeTier();
                    boolean isYoung = (age == AgeTier.BABY || age == AgeTier.CHILD);
                    float baseChance = isYoung ? 0.50f : 0.10f;
                    float traitBonus = (this.getTrait(Trait.CURIOSITY) * 0.10f) +
                                       (this.getTrait(Trait.GLUTTONY) * 0.10f) -
                                       (this.getTrait(Trait.AGGRESSIVENESS) * 0.10f) -
                                       (this.getTrait(Trait.COWARDICE) * 0.10f);

                    float finalChance = Math.max(0.01f, baseChance + traitBonus);

                    if (this.random.nextFloat() < finalChance) {
                        this.tame(player);
                        this.setTarget(null);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }

                if (this instanceof com.geckolib.animatable.GeoEntity geo) {
                    geo.triggerAnim("eat_controller", "eat");
                }
            }
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
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
        return AgeTier.values()[this.entityData.get(AGE_TIER_SYNC)];
    }

    public void setAgeTier(AgeTier tier) {
        this.ageTier = tier;
        this.entityData.set(AGE_TIER_SYNC, (byte) tier.ordinal());
        this.updateStats();
        this.refreshDimensions();
    }

    public float getTrait(Trait trait) { return this.traits.getOrDefault(trait, 0.0f); }
    public float getFeeling(Feeling feeling) { return this.feelings.getOrDefault(feeling, 0.0f); }
    public byte getDominantState() { return this.entityData.get(DOMINANT_STATE); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<PachycephalosaurusEntity>("main_controller", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pachycephalosaurus.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.pachycephalosaurus.idle"));
        }));
        controllers.add(new AnimationController<PachycephalosaurusEntity>("attack_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("attack_1", RawAnimation.begin().thenPlay("animation.pachycephalosaurus.attack_1"))
            .triggerableAnim("attack_2", RawAnimation.begin().thenPlay("animation.pachycephalosaurus.attack_2"))
            .triggerableAnim("charge", RawAnimation.begin().thenPlay("animation.pachycephalosaurus.charge")));
        controllers.add(new AnimationController<PachycephalosaurusEntity>("eat_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("eat", RawAnimation.begin().thenPlay("animation.pachycephalosaurus.eat")));
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
        if (!this.isAlive()) return;

        for (Feeling feeling : Feeling.values()) {
            float currentValue = this.getFeeling(feeling);
            if (currentValue > 0.0f) {
                float traitValue = this.getTrait(getAssociatedTrait(feeling));

                int decayInterval = (int) (1200 * Math.max(0.2f, traitValue));

                if (feeling != Feeling.HUNGER && decayInterval > 0 && this.tickCount % decayInterval == 0) {
                    this.setFeeling(feeling, currentValue - 0.1f);
                }
            }
        }

        if (this.tickCount % 1200 == 0) {
            float gluttony = this.getTrait(Trait.GLUTTONY);
            float hungerIncrease = 0.05f + (gluttony * 0.05f);
            this.setFeeling(Feeling.HUNGER, this.getFeeling(Feeling.HUNGER) + hungerIncrease);
        }

        this.updateDominantState();

        if (this.getAgeTier() != AgeTier.ADULT) {
            this.growthTicks++;

            if (this.growthTicks >= TICKS_TO_GROW) {
                if (this.accumulatedSaturation >= SATURATION_TO_GROW) {
                    this.growUp(level);
                } else {
                    this.applyStuntedGrowthDebuff();
                }
            }
        }
    }

    private void growUp(ServerLevel level) {
        int nextOrdinal = this.getAgeTier().ordinal() + 1;
        if (nextOrdinal < AgeTier.values().length) {

            this.setAgeTier(AgeTier.values()[nextOrdinal]);

            this.growthTicks = 0;
            this.accumulatedSaturation = 0.0f;

            level.broadcastEntityEvent(this, (byte) 14);
        }
    }

    private void applyStuntedGrowthDebuff() {
        int overTicks = this.growthTicks - TICKS_TO_GROW;

        if (this.tickCount % 600 == 0) {
            int severity = Math.min(2, overTicks / 12000);

            this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 800, severity, false, true));

            if (severity > 0) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 800, severity - 1, false, true));
                this.setFeeling(Feeling.ANGER, this.getFeeling(Feeling.ANGER) + 0.05f);
            }
        }
    }

    public float getVisualScale() {
        if (this.getAttributes().hasAttribute(Attributes.SCALE)) {
            return (float) this.getAttributeValue(Attributes.SCALE);
        }

        return this.entityData.get(SCALE);
    }

    public int getGrowthPercent() {
        if (this.getAgeTier() == AgeTier.ADULT) {
            return 100;
        }

        float timeProgress = Math.min(1.0f, (float) this.growthTicks / TICKS_TO_GROW);
        float saturationProgress = Math.min(1.0f, this.accumulatedSaturation / SATURATION_TO_GROW);

        return (int) (((timeProgress + saturationProgress) / 2.0f) * 100.0f);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new FearBehaviorGoal<>(this));
        this.goalSelector.addGoal(2, new AngerBehaviorGoal<>(this, "attack_1"));
        this.goalSelector.addGoal(3, new DinosaurTemptGoal<>(this, 1.1D,
                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.HERBIVORE_FOOD)), false));
        this.goalSelector.addGoal(4, new com.lucas.arch.entity.ai.PachycephalosaurusHungerGoal(this));
        this.goalSelector.addGoal(7, new DinosaurFollowOwnerGoal(this, 1.2D, 24.0F, 8.0F));
        this.goalSelector.addGoal(8, new CuriosityBehaviorGoal<>(this));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }
}