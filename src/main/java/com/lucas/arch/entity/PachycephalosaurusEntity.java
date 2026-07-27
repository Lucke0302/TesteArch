package com.lucas.arch.entity;

import net.minecraft.core.component.DataComponents;
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
import net.minecraft.core.registries.BuiltInRegistries;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

import com.lucas.arch.entity.ai.AngerBehaviorGoal;
import com.lucas.arch.entity.ai.CuriosityBehaviorGoal;
import com.lucas.arch.entity.ai.DinosaurFollowOwnerGoal;
import com.lucas.arch.entity.ai.DinosaurTemptGoal;
import com.lucas.arch.entity.ai.FearBehaviorGoal;
import com.lucas.arch.entity.ai.PachycephalosaurusHungerGoal;
import com.lucas.arch.registry.ModTags;

public class PachycephalosaurusEntity extends AbstractDinosaurEntity {

    /** Multiplicador de saturação: compensa a ausência do bônus de caça do carnívoro. */
    public static final float HERBIVORE_SATURATION_MULTIPLIER = 2.0f;

    private static final int[] COLORS = { 0xFF8B7355, 0xFF6B6B4A, 0xFFA0826D };

    public PachycephalosaurusEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override protected float getBaseHealth() { return 60.0f; }
    @Override protected float getBaseAttackDamage() { return 12.0f; }
    @Override protected float getHitboxScaleRatio() { return 1.5f; }
    @Override protected float getMaxSafeHitboxScale() { return 3.0f; }
    @Override protected int[] getColorPalette() { return COLORS; }
    @Override protected float[] getSpawnScaleRange() { return new float[]{1.0f, 1.3f}; }
    @Override protected float getAdultSpawnScale() { return 1.0f; }
    @Override protected String getColorNbtKey() { return "PachyColor"; }
    @Override protected String getScaleNbtKey() { return "PachyScale"; }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(60.0, 0.28, 6.0);
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
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new FearBehaviorGoal<>(this));
        this.goalSelector.addGoal(2, new AngerBehaviorGoal<>(this, "attack_1"));
        this.goalSelector.addGoal(3, new DinosaurTemptGoal<>(this, 1.1D,
                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.HERBIVORE_FOOD)), false));
        this.goalSelector.addGoal(4, new PachycephalosaurusHungerGoal(this));
        this.goalSelector.addGoal(7, new DinosaurFollowOwnerGoal(this, 1.2D, 24.0F, 8.0F));
        this.goalSelector.addGoal(8, new CuriosityBehaviorGoal<>(this));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }
}