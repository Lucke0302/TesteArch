package com.lucas.arch.entity;

import com.geckolib.animatable.GeoEntity;
import com.lucas.arch.entity.ai.CasualFlightGoal;
import com.lucas.arch.entity.ai.FearFlightGoal;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;

/**
 * Base para dinossauros voadores. Extende AbstractDinosaurEntity com:
 * - Sincronização de estado de voo (IS_FLYING)
 * - FlyingPathNavigation (navegação aérea)
 * - Default dimensions com asa aberta/fechada
 * - Override de calculateEnclosureStress() que faz média de 3 scans
 */
public abstract class AbstractFlyingDinosaurEntity extends AbstractDinosaurEntity implements GeoEntity {

    public static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(
        AbstractFlyingDinosaurEntity.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> IS_DIVING = SynchedEntityData.defineId(
        AbstractFlyingDinosaurEntity.class, EntityDataSerializers.BOOLEAN);

    public int diveEndTick = 0;

    public static final EntityDataAccessor<Float> FLIGHT_ALTITUDE = SynchedEntityData.defineId(
        AbstractFlyingDinosaurEntity.class, EntityDataSerializers.FLOAT);

    protected AbstractFlyingDinosaurEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FearFlightGoal(this));
        this.goalSelector.addGoal(3, new CasualFlightGoal(this));
    }

    public void startSleeping() {
        if (this.isFlying()) {
            this.setFlying(false);
            int groundY = this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, this.blockPosition()).getY();
            this.teleportTo(this.getX(), groundY, this.getZ());
        }
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
        this.entityData.set(IS_FLYING, input.getBooleanOr("IsFlying", false));
        this.entityData.set(FLIGHT_ALTITUDE, input.getFloatOr("FlightAltitude", getFlightAltitude()));
    }


    /**
     * @return altitude padrão de voo em blocos acima do chão.
     */
    public abstract float getFlightAltitude();

    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(IS_FLYING, flying);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
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

        float stress;
        if (avgDist >= requiredRadius) {
            stress = 0f;
        } else {
            stress = 1.0f - (avgDist / requiredRadius);
        }

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

            boolean flying = this.isFlying();
            if (flying && !this.isNoGravity()) {
                this.setNoGravity(true);
                this.navigation = new FlyingPathNavigation(this, this.level());
                this.moveControl = new FlyingMoveControl(this, 20, true);
            } else if (!flying && this.isNoGravity()) {
                this.setNoGravity(false);
                this.navigation = new GroundPathNavigation(this, this.level());
                this.moveControl = new MoveControl(this);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL) && this.isFlying()) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }
}