package com.lucas.arch.entity;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.lucas.arch.entity.ai.AngerBehaviorGoal;
import com.lucas.arch.entity.ai.CuriosityBehaviorGoal;
import com.lucas.arch.entity.ai.DinosaurFollowOwnerGoal;
import com.lucas.arch.entity.ai.DinosaurTemptGoal;
import com.lucas.arch.entity.ai.FearBehaviorGoal;
import com.lucas.arch.entity.ai.HerbivoreHungerGoal;
import com.lucas.arch.entity.ai.NeutralBehaviorGoal;
import com.lucas.arch.entity.ai.SleepBehaviorGoal;
import com.lucas.arch.registry.ModTags;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class ParasaurolophusEntity extends AbstractDinosaurEntity implements HerbivoreDiet {

    /** Multiplicador de saturação: compensa a ausência do bônus de caça do carnívoro. */
    public static final float HERBIVORE_SATURATION_MULTIPLIER = 2.0f;

    private static final int[] COLORS = { 0xFF8B7355, 0xFF6B6B4A, 0xFFA0826D };

    public ParasaurolophusEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override protected float getBaseHealth() { return 80.0f; }
    @Override protected float getBaseAttackDamage() { return 10.0f; }
    @Override protected float getHitboxScaleRatio() { return 1.5f; }
    @Override protected float getMaxSafeHitboxScale() { return 3.0f; }
    @Override protected int[] getColorPalette() { return COLORS; }
    @Override protected float[] getSpawnScaleRange() { return new float[]{1.6f, 1.8f}; }
    @Override protected float getAdultSpawnScale() { return 1.8f; }
    @Override protected String getColorNbtKey() { return "ParasaurColor"; }
    @Override protected String getScaleNbtKey() { return "ParasaurScale"; }

    private int standAnimStartTick = -1;
    private int standMinEndTick = 0;
    private int standCooldownEndTick = 0;

    private static final int STAND_MIN_TICKS = 80;
    private static final int STAND_COOLDOWN_TICKS = 200;

    private boolean isLookingAround = false;
    private int lookAroundEndTick = 0;
    private int nextLookAroundTick = 0;

    private static final int LOOK_AROUND_MIN_HOLD_TICKS = 60;
    private static final int LOOK_AROUND_HOLD_VARIANCE_TICKS = 80;
    private static final int LOOK_AROUND_MIN_COOLDOWN_TICKS = 100;
    private static final int LOOK_AROUND_COOLDOWN_VARIANCE_TICKS = 300;

    // ========================================================================
    //  Atributos
    // ========================================================================

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(80.0, 0.28, 10.0);
    }

    public void feedSaturation(ItemStack foodStack) {
        if (!foodStack.has(DataComponents.FOOD)) return;
        FoodProperties food = foodStack.get(DataComponents.FOOD);
        float value = food.nutrition() * HERBIVORE_SATURATION_MULTIPLIER;
        this.accumulatedSaturation += value;
        float hunger = getFeeling(Feeling.HUNGER);
        setFeeling(Feeling.HUNGER, Math.max(0, hunger - (value / 50.0f)));
    }

    public void grazeSaturation(float baseNutrition) {
        float value = baseNutrition * HERBIVORE_SATURATION_MULTIPLIER;
        this.accumulatedSaturation += value;
        float hunger = getFeeling(Feeling.HUNGER);
        setFeeling(Feeling.HUNGER, Math.max(0, hunger - (value / 50.0f)));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isTame() && !itemStack.is(ModTags.Items.HERBIVORE_FOOD)) {
            return super.mobInteract(player, hand);
        }

        if (itemStack.is(ModTags.Items.HERBIVORE_FOOD)) {
            if (!this.level().isClientSide()) {
                feedSaturation(itemStack);
                tryTameFromFeed(player, 0.10f);
                if (this instanceof com.geckolib.animatable.GeoEntity geo) {
                    geo.triggerAnim("eat_controller", "eat");
                }
            }
            if (!player.getAbilities().instabuild) itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // ========================================================================
    //  Animações (GeckoLib)
    // ========================================================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main_controller", 5, this::movementPredicate));

        controllers.add(new AnimationController<>("attack_controller", 0, test -> PlayState.STOP)
            .triggerableAnim("attack_1", RawAnimation.begin().thenPlay("animation.parasaurolophus.attack")));

        controllers.add(new AnimationController<>("eat_controller", 0, test -> PlayState.STOP)
            .triggerableAnim("eat", RawAnimation.begin().thenPlay("animation.parasaurolophus.eat")));
    }

    /**
     * Extrai o {@link Feeling} dominante do {@code DOMINANT_STATE} sincronizado,
     * ou {@code null} se nenhum feeling ultrapassar o threshold.
     */
    private Feeling getDominantFeeling() {
        byte stateByte = this.getDominantState();
        if (stateByte > 0 && stateByte <= Feeling.values().length) {
            return Feeling.values()[stateByte - 1];
        }
        return null;
    }

    @Override 
    protected boolean isDiurnal() { 
        return true;
    }

    private PlayState movementPredicate(AnimationTest<ParasaurolophusEntity> event) {
        if (this.isSleeping()) {
            boolean isAdult = this.getAgeTier() == AgeTier.ADULT;
            return event.setAndContinue(RawAnimation.begin()
                .thenLoop(isAdult ? "animation.parasaurolophus.sleep_adult" : "animation.parasaurolophus.sleep_baby"));
        }
        if (this.isResting()) {
            boolean isAdult = this.getAgeTier() == AgeTier.ADULT;
            return event.setAndContinue(RawAnimation.begin()
                .thenLoop(isAdult ? "animation.parasaurolophus.sleep_adult" : "animation.parasaurolophus.sleep_baby"));
        }

        boolean isMoving = event.isMoving();
        Feeling dominant = getDominantFeeling();
        updateLookAroundCycle(dominant, isMoving);

        boolean wantsStand = (dominant == Feeling.HUNGER && !isMoving)
                        || (dominant == Feeling.FEAR && isMoving)
                        || this.isLookingAround;

        boolean canStand = wantsStand
            && (dominant == Feeling.FEAR || this.tickCount >= this.standCooldownEndTick);

        boolean useRun = (dominant == Feeling.FEAR && isMoving);

        if (canStand) {
            if (this.standAnimStartTick == -1) {
                this.standAnimStartTick = this.tickCount;
            }
            return event.setAndContinue(RawAnimation.begin()
                .thenPlay("animation.parasaurolophus.stand_up")
                .thenLoop(useRun ? "animation.parasaurolophus.run" : "animation.parasaurolophus.stand"));
        }

        if (this.standAnimStartTick != -1) {
            this.standAnimStartTick = -1;
            if (dominant != Feeling.FEAR) {
                this.standCooldownEndTick = this.tickCount + STAND_COOLDOWN_TICKS
                    + this.random.nextInt(80);
            }
        }

        if (isMoving) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.parasaurolophus.walk"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.parasaurolophus.idle"));
    }

    /**
     * Controla o ciclo periódico de "olhar em volta": quando o dino está neutro e
     * parado, a cada tanto ele entra em {@code isLookingAround} por alguns segundos
     * (stand_up → stand) e depois sai (stand_down → idle), agendando o próximo ciclo.
     * Interrompido automaticamente se ele começar a andar ou algum feeling assumir
     * o controle.
     */
    private void updateLookAroundCycle(Feeling dominantFeeling, boolean isMoving) {
        boolean eligible = dominantFeeling == null && !isMoving;

        if (this.isLookingAround) {
            if (!eligible || this.tickCount >= this.lookAroundEndTick) {
                this.isLookingAround = false;
                this.nextLookAroundTick = this.tickCount + LOOK_AROUND_MIN_COOLDOWN_TICKS
                    + this.random.nextInt(LOOK_AROUND_COOLDOWN_VARIANCE_TICKS);
            }
            return;
        }

        if (eligible && this.tickCount >= this.nextLookAroundTick) {
            this.isLookingAround = true;
            this.lookAroundEndTick = this.tickCount + LOOK_AROUND_MIN_HOLD_TICKS
                + this.random.nextInt(LOOK_AROUND_HOLD_VARIANCE_TICKS);
        }
    }

    // ========================================================================
    //  Goals (IA)
    // ========================================================================

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SleepBehaviorGoal<>(this));
        this.goalSelector.addGoal(0, new NeutralBehaviorGoal<>(this));
        this.goalSelector.addGoal(1, new FearBehaviorGoal<>(this));
        this.goalSelector.addGoal(2, new AngerBehaviorGoal<>(this, "attack_1"));
        this.goalSelector.addGoal(3, new DinosaurTemptGoal<>(this, 1.1D,
                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.HERBIVORE_FOOD)), false));
        this.goalSelector.addGoal(4, new HerbivoreHungerGoal<>(this));
        this.goalSelector.addGoal(7, new DinosaurFollowOwnerGoal(this, 1.2D, 24.0F, 8.0F));
        this.goalSelector.addGoal(8, new CuriosityBehaviorGoal<>(this));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }
}