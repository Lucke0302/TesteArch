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
import com.lucas.arch.entity.ai.FlyingGoal;
import com.lucas.arch.entity.ai.NeutralBehaviorGoal;
import com.lucas.arch.entity.ai.CarnivoreHungerGoal;
import com.lucas.arch.entity.ai.SleepBehaviorGoal;
import com.lucas.arch.registry.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class DimorphodonEntity extends AbstractFlyingDinosaurEntity implements CarnivoreDiet {
    
    private static final int[] COLORS = { 0xFF5A4A3C, 0xFF4C5E44, 0xFF7A4B3A }; 
    public DimorphodonEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override protected float getBaseHealth() { return 20.0f; }
    @Override protected float getBaseAttackDamage() { return 4.0f; }
    @Override protected float getHitboxScaleRatio() { return 1.0f; }
    @Override protected float getMaxSafeHitboxScale() { return 2.0f; }
    @Override protected int[] getColorPalette() { return COLORS; }
    @Override protected float[] getSpawnScaleRange() { return new float[]{0.6f, 0.9f}; }
    @Override protected float getAdultSpawnScale() { return 0.8f; }
    @Override protected String getColorNbtKey() { return "DimorphodonColor"; }
    @Override protected String getScaleNbtKey() { return "DimorphodonScale"; }
    
    @Override protected float getMinEnclosureRadius() { return 10f; }
    
    @Override public float getFlightAltitude() { return 6f; }

    @Override protected boolean isDiurnal() { return true; }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(20.0, 0.25, 4.0)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    public boolean hasDiveAnimation() {
        return true;
    }

    @Override
    public void startDiving() {
        if (!this.level().isClientSide()) {
            this.entityData.set(IS_DIVING, true);
            this.diveEndTick = this.tickCount + 30; 
        }
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
            || entity instanceof net.minecraft.world.entity.animal.rabbit.Rabbit
            || (entity instanceof Player p && !p.isCreative() && !p.isSpectator()
                && p.getMainHandItem().is(ModTags.Items.CARNIVORE_FOOD));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean result = super.doHurtTarget(level, target);
        if (result && target instanceof LivingEntity livingTarget && livingTarget.isDeadOrDying()) {
            ItemStack simulatedMeat = switch (livingTarget) {
                case net.minecraft.world.entity.animal.fish.AbstractFish fish -> new ItemStack(Items.COD);
                case net.minecraft.world.entity.animal.chicken.Chicken chicken -> new ItemStack(Items.CHICKEN);
                case net.minecraft.world.entity.animal.rabbit.Rabbit rabbit -> new ItemStack(Items.RABBIT);
                case Player player -> new ItemStack(Items.COD);
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
                tryTameFromFeed(player, 0.15f); // Mais fácil de domar
            }
            if (!player.getAbilities().instabuild) itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main_controller", 5, this::movementPredicate));
        
        controllers.add(new AnimationController<DimorphodonEntity>("attack_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.dimorphodon.attack")));
            
        controllers.add(new AnimationController<DimorphodonEntity>("survival_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("eat", RawAnimation.begin().thenPlay("animation.dimorphodon.eat"))
            .triggerableAnim("drink", RawAnimation.begin().thenPlay("animation.dimorphodon.drink")));
            
        controllers.add(new AnimationController<DimorphodonEntity>("flight_action_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("dive", RawAnimation.begin().thenPlay("animation.dimorphodon.dive"))
            .triggerableAnim("takeoff", RawAnimation.begin().thenPlay("animation.dimorphodon.takeoff")));
            
        controllers.add(new AnimationController<DimorphodonEntity>("social_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("call", RawAnimation.begin().thenPlay("animation.dimorphodon.call"))
            .triggerableAnim("speak", RawAnimation.begin().thenPlay("animation.dimorphodon.speak"))
            .triggerableAnim("sit", RawAnimation.begin().thenPlay("animation.dimorphodon.sit")));

        controllers.add(new AnimationController<DimorphodonEntity>("climb_controller", 0, state -> PlayState.STOP)
            .triggerableAnim("climb", RawAnimation.begin().thenPlay("animation.dimorphodon.climb"))
            .triggerableAnim("climb_idle", RawAnimation.begin().thenPlay("animation.dimorphodon.!climb_idle")));
    }

    private PlayState movementPredicate(AnimationTest<DimorphodonEntity> event) {
        if (this.isDiving()) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.dimorphodon.dive"));
        }
        
        if (this.isSleeping() || this.isResting()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.sleep_1"));
        }
        
        if (this.isFlying()) {
            if (event.isMoving() && this.getDeltaMovement().lengthSqr() > 0.5) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.fly_fast"));
            }
            if (!event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.idle_air"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.fly"));
        }
        
        if (this.isInWater()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.swim"));
        }
        
        if (event.isMoving()) {
            if (this.isSprinting()) { 
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.run"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.walk"));
        }
        
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.dimorphodon.idle"));
    }

    // --- Goals ---
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SleepBehaviorGoal<>(this));
        this.goalSelector.addGoal(1, new CarnivoreHungerGoal<>(this)); 
        this.goalSelector.addGoal(2, new AngerBehaviorGoal<>(this));
        this.goalSelector.addGoal(3, new FearBehaviorGoal<>(this));
        this.goalSelector.addGoal(4, new FlyingGoal(this));
        this.goalSelector.addGoal(5, new DinosaurTemptGoal<>(this, 1.1D, Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.CARNIVORE_FOOD)), false));
        this.goalSelector.addGoal(6, new DinosaurFollowOwnerGoal(this, 1.2D, 24.0F, 8.0F));
        this.goalSelector.addGoal(7, new CuriosityBehaviorGoal<>(this));
        this.goalSelector.addGoal(8, new NeutralBehaviorGoal<>(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }
}