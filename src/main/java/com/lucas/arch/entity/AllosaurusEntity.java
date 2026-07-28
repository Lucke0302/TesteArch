package com.lucas.arch.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

import com.lucas.arch.entity.ai.AngerBehaviorGoal;
import com.lucas.arch.entity.ai.CarnivoreHungerGoal;
import com.lucas.arch.entity.ai.CuriosityBehaviorGoal;
import com.lucas.arch.entity.ai.DinosaurFollowOwnerGoal;
import com.lucas.arch.entity.ai.DinosaurTemptGoal;
import com.lucas.arch.entity.ai.FearBehaviorGoal;
import com.lucas.arch.entity.ai.NeutralBehaviorGoal;
import com.lucas.arch.entity.ai.SleepBehaviorGoal;
import com.lucas.arch.registry.ModTags;

import com.lucas.arch.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;

public class AllosaurusEntity extends AbstractDinosaurEntity implements CarnivoreDiet {

    private static final int[] COLORS = { 0xFFD97C3A, 0xFF8B5A2B, 0xFF6B8E23 };

    public AllosaurusEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    // --- Hooks de espécie ---
    @Override protected float getBaseHealth() { return 100.0f; }
    @Override protected float getBaseAttackDamage() { return 20.0f; }
    @Override protected float getHitboxScaleRatio() { return 0.9f; }
    @Override protected float getMaxSafeHitboxScale() { return 3.0f; }
    @Override protected int[] getColorPalette() { return COLORS; }
    @Override protected float[] getSpawnScaleRange() { return new float[]{2.7f, 3.5f}; }
    @Override protected float getAdultSpawnScale() { return 3.1f; }
    @Override protected String getColorNbtKey() { return "AllosaurusColor"; }
    @Override protected String getScaleNbtKey() { return "AllosaurusScale"; }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(100.0, 0.3, 10.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ALLO_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ALLO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALLO_DEATH;
    }

    // --- CarnivoreDiet ---
    @Override
    public void feedSaturation(ItemStack foodStack, boolean isHuntBonus) {
        if (!foodStack.has(DataComponents.FOOD)) return;
        FoodProperties food = foodStack.get(DataComponents.FOOD);
        float value = food.nutrition();
        if (isHuntBonus) { value *= 2.0f; this.heal(value); }
        this.accumulatedSaturation += value;
        float hunger = getFeeling(Feeling.HUNGER);
        setFeeling(Feeling.HUNGER, Math.max(0, hunger - (value / 50.0f)));
    }

    @Override
    public boolean isValidPrey(LivingEntity entity) {
        return entity instanceof net.minecraft.world.entity.animal.cow.Cow
            || entity instanceof net.minecraft.world.entity.animal.pig.Pig
            || entity instanceof net.minecraft.world.entity.animal.sheep.Sheep
            || entity instanceof net.minecraft.world.entity.animal.chicken.Chicken
            || (entity instanceof Player p && !p.isCreative() && !p.isSpectator()
                && p.getMainHandItem().is(ModTags.Items.CARNIVORE_FOOD));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean result = super.doHurtTarget(level, target);

        if (result && target instanceof LivingEntity livingTarget && livingTarget.isDeadOrDying()) {
            ItemStack simulatedMeat = switch (livingTarget) {
                case net.minecraft.world.entity.animal.cow.Cow cow -> new ItemStack(Items.BEEF);
                case net.minecraft.world.entity.animal.pig.Pig pig -> new ItemStack(Items.PORKCHOP);
                case net.minecraft.world.entity.animal.sheep.Sheep sheep -> new ItemStack(Items.MUTTON);
                case net.minecraft.world.entity.animal.chicken.Chicken chicken -> new ItemStack(Items.CHICKEN);
                case Player player -> new ItemStack(Items.BEEF);
                case AllosaurusEntity allo -> new ItemStack(com.lucas.arch.registry.ModItems.MEAT_CLUSTER);
                default -> new ItemStack(Items.ROTTEN_FLESH);
            };
            if (simulatedMeat.has(DataComponents.FOOD)) feedSaturation(simulatedMeat, true);
        }
        return result;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isTame() && !itemStack.is(ModTags.Items.CARNIVORE_FOOD)) {
            return super.mobInteract(player, hand);
        }

        if (itemStack.is(ModTags.Items.CARNIVORE_FOOD)) {
            if (!this.level().isClientSide()) {
                feedSaturation(itemStack, false);
                tryTameFromFeed(player, 0.10f);
            }
            if (!player.getAbilities().instabuild) itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AllosaurusEntity>("main_controller", 5, state -> {
            if (this.isSleeping()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.sleep"));
            }
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.walk"));
            }
            if (this.isResting()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.sleep"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.allosaurus.idle"));
        }));
        controllers.add(new AnimationController<AllosaurusEntity>("attack_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.allosaurus.attack")));
        controllers.add(new AnimationController<AllosaurusEntity>("eat_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("eat", RawAnimation.begin().thenPlay("animation.allosaurus.eat")));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SleepBehaviorGoal<>(this));
        this.goalSelector.addGoal(0, new NeutralBehaviorGoal<>(this));
        this.goalSelector.addGoal(1, new FearBehaviorGoal<>(this));
        this.goalSelector.addGoal(2, new AngerBehaviorGoal<>(this));
        this.goalSelector.addGoal(3, new DinosaurTemptGoal<>(this, 1.1D,
                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.CARNIVORE_FOOD)), false));
        this.goalSelector.addGoal(4, new CarnivoreHungerGoal<>(this));
        this.goalSelector.addGoal(7, new DinosaurFollowOwnerGoal(this, 1.2D, 24.0F, 8.0F));
        this.goalSelector.addGoal(8, new CuriosityBehaviorGoal<>(this));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }
}