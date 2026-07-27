package com.lucas.arch.entity;

import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
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
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.AnimationTest;

import com.lucas.arch.entity.ai.AngerBehaviorGoal;
import com.lucas.arch.entity.ai.CuriosityBehaviorGoal;
import com.lucas.arch.entity.ai.DinosaurFollowOwnerGoal;
import com.lucas.arch.entity.ai.DinosaurTemptGoal;
import com.lucas.arch.entity.ai.FearBehaviorGoal;
import com.lucas.arch.entity.ai.HerbivoreHungerGoal;
import com.lucas.arch.registry.ModTags;

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
    private boolean wasStanding = false;

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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main_controller", 5, this::movementPredicate));
        
        controllers.add(new AnimationController<>("attack_controller", 0, test -> PlayState.STOP)
            .triggerableAnim("attack_1", RawAnimation.begin().thenPlay("animation.parasaurolophus.attack")));
            
        controllers.add(new AnimationController<>("eat_controller", 0, test -> PlayState.STOP)
            .triggerableAnim("eat", RawAnimation.begin().thenPlay("animation.parasaurolophus.eat")));
    }

    private PlayState movementPredicate(AnimationTest<ParasaurolophusEntity> event) {
        boolean isMoving = event.isMoving();
        
        byte dominantStateByte = this.getDominantState();
        Feeling[] feelings = Feeling.values();
        
        Feeling dominantFeeling;
        if (dominantStateByte >= 0 && dominantStateByte < feelings.length) {
            dominantFeeling = feelings[dominantStateByte];
        } else {
            dominantFeeling = feelings[0]; 
        }

        if (dominantFeeling == Feeling.FEAR && isMoving) {
            this.wasStanding = true;
            return event.setAndContinue(RawAnimation.begin()
                .thenPlay("animation.parasaurolophus.stand_up")
                .thenLoop("animation.parasaurolophus.run"));
        }

        if (dominantFeeling == Feeling.HUNGER && !isMoving) {
            this.wasStanding = true;
            return event.setAndContinue(RawAnimation.begin()
                .thenPlay("animation.parasaurolophus.stand_up")
                .thenLoop("animation.parasaurolophus.stand"));
        }

        if (isMoving) {
            if (this.wasStanding) {
                this.wasStanding = false;
                return event.setAndContinue(RawAnimation.begin()
                    .thenPlay("animation.parasaurolophus.stand_down")
                    .thenLoop("animation.parasaurolophus.walk"));
            } else {
                // Caminhada normal
                return event.setAndContinue(RawAnimation.begin()
                    .thenLoop("animation.parasaurolophus.walk"));
            }
        }

        if (this.wasStanding) {
            this.wasStanding = false;
            return event.setAndContinue(RawAnimation.begin()
                .thenPlay("animation.parasaurolophus.stand_down")
                .thenLoop("animation.parasaurolophus.idle"));
        }

        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.parasaurolophus.idle"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

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