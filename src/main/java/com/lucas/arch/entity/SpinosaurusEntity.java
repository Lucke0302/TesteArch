package com.lucas.arch.entity;

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
import com.lucas.arch.registry.ModSounds;
import com.lucas.arch.registry.ModTags;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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

public class SpinosaurusEntity extends AbstractDinosaurEntity implements CarnivoreDiet {

    private static final int[] COLORS = { 0xFF3F5F6B, 0xFF4A6741, 0xFF6B4A3F };

    public SpinosaurusEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override 
    protected boolean isDiurnal() { 
        return true;
    }

    @Override protected float getBaseHealth() { return 90.0f; }
    @Override protected float getBaseAttackDamage() { return 16.0f; }
    @Override protected float getHitboxScaleRatio() { return 1f; }
    @Override protected float getMaxSafeHitboxScale() { return 3.0f; }
    @Override protected int[] getColorPalette() { return COLORS; }
    @Override protected float[] getSpawnScaleRange() { return new float[]{2.6f, 3.2f}; }
    @Override protected float getAdultSpawnScale() { return 3.0f; }
    @Override protected String getColorNbtKey() { return "SpinosaurusColor"; }
    @Override protected String getScaleNbtKey() { return "SpinosaurusScale"; }
    @Override protected float getMinEnclosureRadius() { return 30f; }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(90.0, 0.28, 16.0);
    }

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
        return entity instanceof net.minecraft.world.entity.animal.fish.AbstractFish
            || entity instanceof net.minecraft.world.entity.animal.chicken.Chicken
            || entity instanceof net.minecraft.world.entity.animal.sheep.Sheep
            || entity instanceof net.minecraft.world.entity.animal.pig.Pig
            || (entity instanceof Player p && !p.isCreative() && !p.isSpectator()
                && p.getMainHandItem().is(ModTags.Items.CARNIVORE_FOOD));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean result = super.doHurtTarget(level, target);
        if (result && target instanceof LivingEntity livingTarget && livingTarget.isDeadOrDying()) {
            ItemStack simulatedFish = new ItemStack(Items.COD);
            if (simulatedFish.has(DataComponents.FOOD)) feedSaturation(simulatedFish, true);
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
        controllers.add(new AnimationController<SpinosaurusEntity>("main_controller", 5, state -> {
            if (this.isSleeping()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.spinosaurus.sleep"));
            }
            if (state.isMoving() && this.isInWater()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.spinosaurus.swim_underwater"));
            }
            else if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.spinosaurus.walk"));
            }
            if (this.isResting()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.spinosaurus.sleep"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.spinosaurus.idle"));
        }));
        controllers.add(new AnimationController<SpinosaurusEntity>("attack_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.spinosaurus.attack")));
        controllers.add(new AnimationController<SpinosaurusEntity>("eat_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("eat", RawAnimation.begin().thenPlay("animation.spinosaurus.eat")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SPINO_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SPINO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SPINO_DEATH;
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