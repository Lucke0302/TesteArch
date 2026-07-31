package com.lucas.arch.entity;

import com.geckolib.animatable.GeoEntity;
import com.lucas.arch.entity.ai.FlyingGoal;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth; 
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFlyingDinosaurEntity extends AbstractDinosaurEntity implements GeoEntity {

    public static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(
        AbstractFlyingDinosaurEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_DIVING = SynchedEntityData.defineId(
        AbstractFlyingDinosaurEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> FLIGHT_ALTITUDE = SynchedEntityData.defineId(
        AbstractFlyingDinosaurEntity.class, EntityDataSerializers.FLOAT);

    public int diveEndTick = 0;

    private final GroundPathNavigation groundNavigation;
    private final FlyingPathNavigation flyingNavigation;
    private final MoveControl groundMoveControl;
    private final FlyingMoveControl flyingMoveControl;
    private boolean wasFlying = false;

    protected AbstractFlyingDinosaurEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
        this.flyingNavigation = new FlyingPathNavigation(this, level);
        this.groundMoveControl = new MoveControl(this);
        
        // Usando o controlador de voo nativo e confiável do Vanilla
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);
        
        this.navigation = this.groundNavigation;
        this.moveControl = this.groundMoveControl;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FlyingGoal(this));
    }

    @Override
    public boolean canRest() {
        return super.canRest() && !this.isFlying();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(FLIGHT_ALTITUDE, getFlightAltitude());
        builder.define(IS_DIVING, false);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsFlying", this.entityData.get(IS_FLYING));
        output.putFloat("FlightAltitude", this.entityData.get(FLIGHT_ALTITUDE));
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        
        this.setFlying(input.getBooleanOr("IsFlying", false)); 
        
        this.entityData.set(FLIGHT_ALTITUDE, input.getFloatOr("FlightAltitude", getFlightAltitude()));
    }

    public abstract float getFlightAltitude();

    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        boolean wasFlyingBefore = this.entityData.get(IS_FLYING);
        this.entityData.set(IS_FLYING, flying);

        this.entityData.set(IS_FLYING, flying);
        if (flying) {
            this.setSleeping(false);
            this.setResting(false);
        }
        
        if (flying != wasFlyingBefore) {
            if (flying) {
                this.setNoGravity(true);
                this.navigation = this.flyingNavigation;
                this.moveControl = this.flyingMoveControl;
                this.setPose(Pose.STANDING);
            } else {
                this.setNoGravity(false);
                this.navigation = this.groundNavigation;
                this.moveControl = this.groundMoveControl;
                this.setResting(false);
                this.setSleeping(false);
                
                this.fallDistance = 0.0F; 
            }
            this.getNavigation().stop();
        }
        this.wasFlying = flying;
    }

    public boolean isDiving() {
        return this.entityData.get(IS_DIVING);
    }

    public void startDiving() {
        this.setFlying(false);
    }

    public boolean hasDiveAnimation() {
        return false;
    }

    public void steerTo(double x, double y, double z, double speedModifier) {
        if (this.tickCount % 20 == 0) {
            System.out.println("[QUETZAL DEBUG] steerTo() -> Direcionando para X: " + x + " Y: " + y + " Z: " + z + " | Distância até o alvo: " + this.distanceToSqr(x, y, z));
        }
        this.moveControl.setWantedPosition(x, y, z, speedModifier);
    }

    public void startSleeping() {
        if (this.isFlying()) {
            this.setFlying(false);
            int groundY = this.level().getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                this.blockPosition()
            ).getY();
            this.teleportTo(this.getX(), groundY, this.getZ());
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions base = super.getDefaultDimensions(pose);
        if (this.isFlying()) {
            return base.scale(1.4f, 1.0f);
        }
        return base;
    }

    @Override
    protected void calculateEnclosureStress() {
        float requiredRadius = getMinEnclosureRadius();
        if (requiredRadius <= 0f) return;

        float groundDist = scanHorizontalRadius(this.blockPosition().getY());
        int flightY = this.blockPosition().getY() + (int) getFlightAltitude();
        float flightDist = scanHorizontalRadius(flightY);
        float verticalClearance = scanVerticalClearance();

        float avgDist = (groundDist * 0.4f) + (flightDist * 0.4f) + (verticalClearance * 0.2f);

        float stress = (avgDist >= requiredRadius) ? 0f : 1.0f - (avgDist / requiredRadius);
        setFeeling(Feeling.STRESS, net.minecraft.util.Mth.clamp(stress, 0.0f, 1.0f));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isDiving() && this.tickCount >= this.diveEndTick) {
                this.entityData.set(IS_DIVING, false);
                this.setFlying(false);
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isDiving()) {
            this.moveRelative(0.1F, travelVector);
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.15D, 0));
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            return;
        }
        
        super.travel(travelVector);
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            return false;
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) && this.isFlying()) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }
}