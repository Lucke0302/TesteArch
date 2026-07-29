package com.lucas.arch.entity;

import java.util.EnumMap;

import org.jetbrains.annotations.Nullable;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.util.GeckoLibUtil;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Base comum a todas as entidades "sencientes" do mod (Feelings/Traits/Growth/Scale/Tame).
 * Extraído de AllosaurusEntity + PachycephalosaurusEntity — NÃO altere a mecânica sem
 * revisar as duas subclasses e o BehaviorResolver.
 *
 * O que a subclasse ainda precisa fazer:
 *  - registerGoals() (composição das fuzzy goals + tempt/follow/stroll)
 *  - mobInteract() (dieta específica: qual tag de comida, chance de tame)
 *  - feedSaturation(ItemStack, boolean bonus) — carnívoro tem hunt bonus, herbívoro não
 *  - registerControllers() (GeckoLib, animações específicas do modelo)
 *  - doHurtTarget()/hurtServer() extras se a espécie tiver efeito colateral de combate
 *
 * NOTA (correção 2026-07-28): isSleeping/isResting foram promovidos de campos Java simples
 * para EntityDataAccessor<Boolean> sincronizados. Antes disso, tickTranquilizer() e
 * NeutralBehaviorGoal#setResting() alteravam apenas a instância server-side da entidade —
 * a instância client-side (a que o GeckoLib AnimationController realmente consulta em
 * registerControllers()) nunca recebia a mudança, então a lógica de sono/descanso
 * funcionava (navegação parava, etc.) mas a animação de "sleep" nunca era ativada.
 * Ver README.md (seção "Sincronização de Estado Visual") e CODEMAP.md (2.2.1) para mais detalhes.
 */
public abstract class AbstractDinosaurEntity extends TamableAnimal implements GeoEntity, FeelingDrivenEntity {

    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Byte> DOMINANT_STATE = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> IS_MALE = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Byte> AGE_TIER_SYNC = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.BYTE);

    public static final EntityDataAccessor<Boolean> IS_SLEEPING_SYNC = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RESTING_SYNC = SynchedEntityData.defineId(AbstractDinosaurEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float DOMINANT_STATE_THRESHOLD = 0.3f;
    protected static final int TICKS_TO_GROW = 1200;
    protected static final float SATURATION_TO_GROW = 4.0f;

    protected final EnumMap<Trait, Float> traits = new EnumMap<>(Trait.class);
    protected final EnumMap<Feeling, Float> feelings = new EnumMap<>(Feeling.class);
    protected AgeTier ageTier = AgeTier.BABY;
    protected float baseScale;
    protected float humanAffinity = 0.5f;
    protected float geneticStatMultiplier = 1.0f;
    protected int growthTicks = 0;
    protected float accumulatedSaturation = 0.0f;
    private int attachedDarts = 0;
    private int tranquilizerTicks = 0;

    protected AbstractDinosaurEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.FIRE, -1.0F);
        for (Feeling feeling : Feeling.values()) this.feelings.put(feeling, 0.0f);
    }

    // --- Hooks que cada espécie define ---
    protected abstract float getBaseHealth();
    protected abstract float getBaseAttackDamage();
    protected abstract boolean isDiurnal();
    protected abstract float getHitboxScaleRatio();
    protected abstract float getMaxSafeHitboxScale();
    protected abstract int[] getColorPalette();
    protected abstract float[] getSpawnScaleRange();
    protected abstract float getAdultSpawnScale();
    protected abstract String getColorNbtKey();
    protected abstract String getScaleNbtKey();

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions vanilla = super.getDefaultDimensions(pose);
        float visualScale = this.getVisualScale();
        if (visualScale <= 0f) return vanilla;
        float capped = Math.min(visualScale, getMaxSafeHitboxScale());
        float relativeRatio = (capped * getHitboxScaleRatio()) / visualScale;
        return vanilla.scale(relativeRatio);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, 0xFFFFFFFF);
        builder.define(SCALE, 1.0f);
        builder.define(DOMINANT_STATE, (byte) 0);
        builder.define(IS_MALE, true);
        builder.define(AGE_TIER_SYNC, (byte) AgeTier.BABY.ordinal());
        builder.define(IS_SLEEPING_SYNC, false);
        builder.define(IS_RESTING_SYNC, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(getColorNbtKey(), this.entityData.get(COLOR));
        output.putFloat(getScaleNbtKey(), this.baseScale);
        output.putBoolean("IsMale", this.entityData.get(IS_MALE));
        output.putString("AgeTier", this.ageTier.name());
        output.putFloat("HumanAffinity", this.humanAffinity);
        output.putFloat("GeneticStatMultiplier", this.geneticStatMultiplier);
        output.putInt("GrowthTicks", this.growthTicks);
        output.putFloat("AccumulatedSaturation", this.accumulatedSaturation);
        for (Trait t : Trait.values()) output.putFloat("Trait_" + t.name(), this.traits.getOrDefault(t, 0.0f));
        for (Feeling f : Feeling.values()) output.putFloat("Feeling_" + f.name(), this.feelings.getOrDefault(f, 0.0f));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(COLOR, input.getIntOr(getColorNbtKey(), 0xFFFFFFFF));
        this.entityData.set(IS_MALE, input.getBooleanOr("IsMale", true));
        this.humanAffinity = input.getFloatOr("HumanAffinity", 0.5f);
        this.geneticStatMultiplier = input.getFloatOr("GeneticStatMultiplier", 1.0f);

        float scaleFromNbt = input.getFloatOr(getScaleNbtKey(), Float.NaN);
        String ageTierFromNbt = input.getStringOr("AgeTier", "");
        boolean hasCustomScale = !Float.isNaN(scaleFromNbt);
        boolean hasCustomAgeTier = !ageTierFromNbt.isBlank();

        this.growthTicks = input.getIntOr("GrowthTicks", 0);
        this.accumulatedSaturation = input.getFloatOr("AccumulatedSaturation", 0.0f);

        if (hasCustomScale) this.baseScale = scaleFromNbt;
        if (hasCustomAgeTier) {
            this.ageTier = AgeTier.valueOf(ageTierFromNbt);
            this.entityData.set(AGE_TIER_SYNC, (byte) this.ageTier.ordinal());
        }

        for (Trait t : Trait.values()) this.traits.put(t, input.getFloatOr("Trait_" + t.name(), 0.0f));
        for (Feeling f : Feeling.values()) this.feelings.put(f, input.getFloatOr("Feeling_" + f.name(), 0.0f));

        this.entityData.set(IS_SLEEPING_SYNC, false);
        this.entityData.set(IS_RESTING_SYNC, false);

        if (hasCustomScale || hasCustomAgeTier) this.updateStats();
        else this.entityData.set(SCALE, this.getVisualScale());

        this.refreshDimensions();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.entityData.set(IS_MALE, this.random.nextBoolean());

        float[] range = getSpawnScaleRange();
        if (reason == EntitySpawnReason.COMMAND || reason == EntitySpawnReason.SPAWN_ITEM_USE) {
            this.setAgeTier(AgeTier.ADULT);
            this.baseScale = getAdultSpawnScale();
        } else {
            this.setAgeTier(AgeTier.BABY);
            this.baseScale = range[0] + (this.random.nextFloat() * (range[1] - range[0]));
        }

        float totalPoints = Trait.values().length / 2.0f;
        float[] rolls = new float[Trait.values().length];
        float sum = 0;
        for (int i = 0; i < rolls.length; i++) { rolls[i] = this.random.nextFloat(); sum += rolls[i]; }
        int index = 0;
        for (Trait trait : Trait.values()) {
            this.traits.put(trait, Math.min((rolls[index++] / sum) * totalPoints, 1.0f));
        }

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

    public static AttributeSupplier.Builder baseAttributes(double maxHealth, double moveSpeed, double attackDamage) {
        return Animal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, maxHealth)
            .add(Attributes.MOVEMENT_SPEED, moveSpeed)
            .add(Attributes.ATTACK_DAMAGE, attackDamage)
            .add(Attributes.SCALE, 1.0)
            .add(Attributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanFloat(true);
        nav.setMaxVisitedNodesMultiplier(this.getBbWidth() > 2.0F ? 2.0F : 1.0F);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount == 1 && !this.level().isClientSide() && this.entityData.get(COLOR) == 0xFFFFFFFF) {
            int[] palette = getColorPalette();
            this.entityData.set(COLOR, palette[this.random.nextInt(palette.length)]);
            this.refreshDimensions();
        }
    }

    public void updateStats() {
        float ageMultiplier = this.ageTier.getScaleMultiplier();
        float effectiveScale = this.baseScale * ageMultiplier;

        if (this.getAttribute(Attributes.SCALE) != null) this.getAttribute(Attributes.SCALE).setBaseValue(effectiveScale);
        this.entityData.set(SCALE, effectiveScale);

        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getBaseAttackDamage() * this.geneticStatMultiplier * ageMultiplier);
        }
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            double oldMax = this.getMaxHealth();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getBaseHealth() * this.geneticStatMultiplier * ageMultiplier);
            if (this.getHealth() > 0 && oldMax > 0) {
                this.setHealth((float) (this.getHealth() * (this.getMaxHealth() / oldMax)));
            }
        }
    }

    protected void updateDominantState() {
        Feeling dominant = null;
        float highest = DOMINANT_STATE_THRESHOLD;

        float aggro = getTrait(Trait.AGGRESSIVENESS), coward = getTrait(Trait.COWARDICE);
        float glut = getTrait(Trait.GLUTTONY), curio = getTrait(Trait.CURIOSITY);

        for (Feeling feeling : Feeling.values()) {
            float raw = getFeeling(feeling);
            float multiplier = 1.0f;
            switch (feeling) {
                case ANGER -> multiplier += (aggro * 0.6f) + (curio * aggro * 0.4f) - (coward * 0.5f);
                case FEAR -> multiplier += (coward * 0.6f) - (aggro * 0.5f) - (curio * 0.3f);
                case HUNGER -> multiplier += (glut * 0.5f) + (aggro * 0.3f);
                case CURIOSITY -> multiplier += (curio * 0.5f) + (glut * curio * 0.3f) - (coward * 0.6f);
            }
            float effective = raw * Math.max(0.1f, multiplier);
            if (effective > highest) { highest = effective; dominant = feeling; }
        }

        byte newState = (byte) (dominant == null ? 0 : dominant.ordinal() + 1);
        if (this.entityData.get(DOMINANT_STATE) != newState) this.entityData.set(DOMINANT_STATE, newState);
    }

    @Override
    public void setFeeling(Feeling feeling, float value) {
        this.feelings.put(feeling, Mth.clamp(value, 0.0f, 1.0f));
    }

    protected Trait getAssociatedTrait(Feeling feeling) {
        return switch (feeling) {
            case ANGER -> Trait.AGGRESSIVENESS;
            case FEAR -> Trait.COWARDICE;
            case CURIOSITY -> Trait.CURIOSITY;
            case HUNGER -> Trait.GLUTTONY;
        };
    }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    public int getColor() { return this.entityData.get(COLOR); }
    public boolean isMale() { return this.entityData.get(IS_MALE); }
    public AgeTier getAgeTier() { return AgeTier.values()[this.entityData.get(AGE_TIER_SYNC)]; }

    public void setAgeTier(AgeTier tier) {
        this.ageTier = tier;
        this.entityData.set(AGE_TIER_SYNC, (byte) tier.ordinal());
        this.updateStats();
        this.refreshDimensions();
    }

    @Override public float getTrait(Trait trait) { return this.traits.getOrDefault(trait, 0.0f); }
    @Override public float getFeeling(Feeling feeling) { return this.feelings.getOrDefault(feeling, 0.0f); }
    @Override public byte getDominantState() { return this.entityData.get(DOMINANT_STATE); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }

    public boolean isTranquilized() {
        int requiredDoses = (int) Math.ceil(this.getBbHeight());
        return this.attachedDarts >= requiredDoses && this.isSleeping();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isInvulnerableTo(level, source)) {
            if (this.isSleeping()) {
                this.setSleeping(false);
                this.attachedDarts = 0;
                this.tranquilizerTicks = 0;
                this.triggerAnim("main_controller", "idle");
            }
            if (this.isResting()) {
                this.setResting(false);
                this.triggerAnim("main_controller", "idle");
            }
        }

        boolean wasHurt = super.hurtServer(level, source, amount);
        if (wasHurt && !this.isInvulnerableTo(level, source)) {
            float impact = Math.min(amount / 20.0f, 1.0f);
            this.setFeeling(Feeling.ANGER, this.getFeeling(Feeling.ANGER) + (impact * getTrait(Trait.AGGRESSIVENESS)));
            this.setFeeling(Feeling.FEAR, this.getFeeling(Feeling.FEAR) + (impact * getTrait(Trait.COWARDICE)));
        }
        return wasHurt;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isAlive()) return;

        for (Feeling feeling : Feeling.values()) {
            float current = getFeeling(feeling);
            if (current > 0.0f) {
                float traitValue = getTrait(getAssociatedTrait(feeling));
                int decayInterval = (int) (1200 * Math.max(0.2f, traitValue));
                if (feeling != Feeling.HUNGER && decayInterval > 0 && this.tickCount % decayInterval == 0) {
                    setFeeling(feeling, current - 0.1f);
                }
            }
        }

        if (this.tickCount % 1200 == 0) {
            float gluttony = getTrait(Trait.GLUTTONY);
            setFeeling(Feeling.HUNGER, getFeeling(Feeling.HUNGER) + 0.05f + (gluttony * 0.05f));
        }

        this.tickTranquilizer();
        updateDominantState();

        long timeOfDay = level.getDefaultClockTime() % 24000L;
        
        boolean isNight = timeOfDay >= 13000 && timeOfDay < 23000;
        
        boolean isSleepTime = isDiurnal() ? isNight : !isNight;

        if (this.isSleeping()) {
            boolean isTranquilized = this.attachedDarts >= Math.ceil(this.getBbHeight()) && this.tranquilizerTicks >= 300;
            
            if (!isTranquilized) {
                if (!isSleepTime || this.getFeeling(Feeling.HUNGER) >= 0.8f) {
                    this.setSleeping(false);
                    this.triggerAnim("main_controller", "idle");
                }
            }
        } else {
            if (isSleepTime) {
                byte domState = this.getDominantState();
                float feelingVal = domState > 0 ? getFeeling(Feeling.values()[domState - 1]) : 0.0f;
                
                if (domState == 0 || feelingVal <= 0.5f) {
                    if (this.tickCount % 200 == 0 && this.random.nextFloat() < 0.20f) {
                        this.setSleeping(true);
                        this.getNavigation().stop();
                        this.setTarget(null);
                    }
                }
            }
        }

        if (getAgeTier() != AgeTier.ADULT) {
            this.growthTicks++;
            if (this.growthTicks >= TICKS_TO_GROW) {
                if (this.accumulatedSaturation >= SATURATION_TO_GROW) growUp(level);
                else applyStuntedGrowthDebuff();
            }
        }
    }

    protected void growUp(ServerLevel level) {
        int next = getAgeTier().ordinal() + 1;
        if (next < AgeTier.values().length) {
            setAgeTier(AgeTier.values()[next]);
            this.growthTicks = 0;
            this.accumulatedSaturation = 0.0f;
            level.broadcastEntityEvent(this, (byte) 14);
        }
    }

    public void addDartDose() {
        this.attachedDarts++;
    }

    /**
     * @return true se estiver dormindo por tranquilizante.
     * Lê o valor sincronizado (EntityDataAccessor), então funciona corretamente
     * tanto na instância server-side (lógica) quanto na client-side (GeckoLib).
     */
    public boolean isSleeping() {
        return this.entityData.get(IS_SLEEPING_SYNC);
    }

    /**
     * @return true se estiver descansando/deitado (comportamento passivo por trait).
     * Também sincronizado — ver nota da classe.
     */
    public boolean isResting() {
        return this.entityData.get(IS_RESTING_SYNC);
    }

    /**
     * Define se o dinossauro está descansando/deitado (comportamento passivo por trait).
     * Chamado a partir de NeutralBehaviorGoal. Só deve ser chamado no lado servidor;
     * o valor é replicado automaticamente para o cliente via SynchedEntityData.
     */
    public void setResting(boolean resting) {
        this.entityData.set(IS_RESTING_SYNC, resting);
    }

    private void setSleeping(boolean sleeping) {
        this.entityData.set(IS_SLEEPING_SYNC, sleeping);
    }

    protected void tickTranquilizer() {
        int requiredDoses = (int) Math.ceil(this.getBbHeight()); 
        
        if (this.attachedDarts >= requiredDoses) {
            this.tranquilizerTicks++;
            
            if (this.tranquilizerTicks == 300) {
                this.setSleeping(true);
                this.getNavigation().stop();
                this.setTarget(null);
                this.triggerAnim("main_controller", "sleep"); 
            }
            
            if (this.tranquilizerTicks > 3600) { 
                this.setSleeping(false);
                this.attachedDarts = 0;
                this.tranquilizerTicks = 0;
                this.triggerAnim("main_controller", "idle");
            }
        } else {
            if (this.attachedDarts > 0 && this.tickCount % 600 == 0) {
                this.attachedDarts--;
            }
        }
    }

    protected void applyStuntedGrowthDebuff() {
        int overTicks = this.growthTicks - TICKS_TO_GROW;
        if (this.tickCount % 600 == 0) {
            int severity = Math.min(2, overTicks / 12000);
            this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 800, severity, false, true));
            if (severity > 0) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 800, severity - 1, false, true));
                setFeeling(Feeling.ANGER, getFeeling(Feeling.ANGER) + 0.05f);
            }
        }
    }

    public float getVisualScale() {
        if (this.getAttributes().hasAttribute(Attributes.SCALE)) return (float) this.getAttributeValue(Attributes.SCALE);
        return this.entityData.get(SCALE);
    }

    public int getGrowthPercent() {
        if (getAgeTier() == AgeTier.ADULT) return 100;
        float timeProgress = Math.min(1.0f, (float) this.growthTicks / TICKS_TO_GROW);
        float saturationProgress = Math.min(1.0f, this.accumulatedSaturation / SATURATION_TO_GROW);
        return (int) (((timeProgress + saturationProgress) / 2.0f) * 100.0f);
    }

    protected boolean tryTameFromFeed(Player player, float baseChance) {
        if (this.isTame()) return false;
        AgeTier age = getAgeTier();
        boolean isYoung = (age == AgeTier.BABY || age == AgeTier.CHILD);
        float chance = isYoung ? Math.max(baseChance, 0.50f) : baseChance;
        float traitBonus = (getTrait(Trait.CURIOSITY) * 0.10f) + (getTrait(Trait.GLUTTONY) * 0.10f)
                - (getTrait(Trait.AGGRESSIVENESS) * 0.10f) - (getTrait(Trait.COWARDICE) * 0.10f);
        float finalChance = Math.max(0.01f, chance + traitBonus);

        boolean tamed = this.random.nextFloat() < finalChance;
        if (tamed) { this.tame(player); this.setTarget(null); }
        this.level().broadcastEntityEvent(this, (byte) (tamed ? 7 : 6));
        return tamed;
    }
}