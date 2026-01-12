package net.commoble.froglins;

import org.jspecify.annotations.Nullable;

import net.commoble.froglins.ai.JumpSometimesGoal;
import net.commoble.froglins.ai.LeapAtTargetGoalNoFriction;
import net.commoble.froglins.ai.MoveToWaterGoal;
import net.commoble.froglins.ai.PredicatedGoal;
import net.commoble.froglins.ai.SinkInWaterGoal;
import net.commoble.froglins.ai.SwimToTargetGoal;
import net.commoble.froglins.ai.SwimUpOrDownGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

public class FroglinEntity extends Monster
{
	
	public static final String FULLNESS = "fullness";
	private int fullness = 0;
	public int getFullness() { return this.fullness; }
	public void setFullness(int value) { this.fullness = value; }
	public void addFullness(int value) { this.fullness += value; }
	public void decrementFullness() { --this.fullness; }
	
	public static final String EGGS = "eggs";
	private int eggs = 0;
	public int getEggs() { return this.eggs; }
	public void setEggs(int value) { this.eggs = value; }
	public void addEggs(int value) { this.eggs += value; }
	
	// make a reference to this so we can check if this is running or not
	// we can't set it here because registerGoals is called from MobEntity's constructor
	// i.e. *before* FroglinEntity's constructor
	private MoveToWaterGoal moveToWaterGoal;

	public FroglinEntity(EntityType<? extends FroglinEntity> type, Level level)
	{
		super(type, level);
		this.moveControl = new FroglinMovementController(this);
	}
	
	////// Entity Properties //////

	public static AttributeSupplier.Builder createAttributes()
	{
//		return MonsterEntity.createMonsterAttributes(); // standard attributes
		return Monster.createMonsterAttributes()
			.add(Attributes.FOLLOW_RANGE, 20.0D)	// zombies are 35, monster default is 16
			.add(Attributes.MOVEMENT_SPEED, 0.3F)	// zombies are 0.23F, players are 0.7F?
			.add(NeoForgeMod.SWIM_SPEED, 1.5F)
			.add(Attributes.ATTACK_DAMAGE, 4.0D);
	}
	
	public static boolean canRandomlySpawn(EntityType<FroglinEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource rand)
	{
		int minY = level.getSeaLevel() - 5;
		int y = pos.getY();
		return level.getDifficulty() != Difficulty.PEACEFUL
			&& y >= minY
			&& Monster.isDarkEnoughToSpawn(level, pos, rand)
			&& (reason == EntitySpawnReason.SPAWNER || level.getBlockState(pos).getFluidState().is(FluidTags.WATER));
	}


	@Override
	protected PathNavigation createNavigation(Level level)
	{
		return new AmphibiousPathNavigation(this,level);
	}
	
	@Override
	protected void registerGoals()
	{
		this.moveToWaterGoal = new MoveToWaterGoal(this, 1.2D);
		
		this.goalSelector.addGoal(1, new SinkInWaterGoal(this)); // MOVE, JUMP
		this.goalSelector.addGoal(2, this.moveToWaterGoal);	// MOVE
		this.goalSelector.addGoal(2, new SwimUpOrDownGoal(this));	// JUMP
		this.goalSelector.addGoal(2, new SwimToTargetGoal(this));
		this.goalSelector.addGoal(3, new PredicatedGoal<>(this,
			new FloatGoal(this),
			FroglinEntity::wantsToHunt));	// JUMP
		this.goalSelector.addGoal(3, new PredicatedGoal<>(this,
			new PanicGoal(this, 2D),
			FroglinEntity::wantsToRunAway));	// MOVE
		this.goalSelector.addGoal(4, new PredicatedGoal<>(this,
			new LeapAtTargetGoalNoFriction(this, 1.0F),
			frog -> !frog.isInWater() && frog.getPose() == Pose.CROUCHING));
		this.goalSelector.addGoal(4, new PredicatedGoal<>(this,
			new LeapAtTargetGoal(this, 0.4F),	// value is y-power of jump
			frog -> !frog.isInWater()));	// MOVE, JUMP
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1D, false));	// MOVE, LOOK
		this.goalSelector.addGoal(6, new JumpSometimesGoal(this));	// JUMP
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1D, 4)); // MOVE
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));	// LOOK
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));	// MOVE, LOOK
		
		// TODO good idea but make sure they don't interfere with other ai (particularly hiding)
//		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, OcelotEntity.class, 6.0F, 1.0D, 1.2D));
//		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, CatEntity.class, 6.0F, 1.0D, 1.2D));
		
		// TODO set calls for help?
		this.targetSelector.addGoal(2, new PredicatedGoal<>(this,
			new HurtByTargetGoal(this).setAlertOthers(),
			FroglinEntity::wantsToRetaliate));
		this.targetSelector.addGoal(3, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, Mob.class, 20, false, false, (entity,serverLevel) -> entity.getType().is(Froglins.EDIBLE_FISH_TAG)),
			FroglinEntity::wantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, Mob.class, 40, false, false, (entity,serverLevel) -> entity.getType().is(Froglins.EDIBLE_ANIMALS_TAG)),
			FroglinEntity::wantsToHunt));
		this.targetSelector.addGoal(5, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, Player.class, 100, false, false, null),
			FroglinEntity::wantsToHunt));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer)
	{
		return this.noActionTime > this.fullness && super.removeWhenFarAway(distanceToClosestPlayer);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return Froglins.FROGLIN_SOUND_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return Froglins.FROGLIN_SOUND_DEATH.get();
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return Froglins.FROGLIN_SOUND_HURT.get();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(Froglins.FROGLIN_SOUND_STEP.get(), 0.15F, 1.0F);
	}

	@Override
	public boolean canBreatheUnderwater()
	{
		return true;
	}

	// called during random spawn attempts, after the entity instance is created, but before it is canonically added to the level
	// normally this returns false if the entity is in water, or if the entity is touching another entity
	// water mobs must override this to skip the water check
	@Override
	public boolean checkSpawnObstruction(LevelReader level)
	{
		return level.isUnobstructed(this);
	}

	// affects how fast you stop moving after you stop moving (lower numbers = stop moving sooner)
	@Override
	protected float getWaterSlowDown()
	{
		return 0.96F;	// same as skeleton horses (why do skeleton horses have high hydrodynamics?)
	}


	@Override
	protected float getJumpPower()
	{
		return (1F + (float)this.random.nextGaussian()*0.5F)
			* (this.getPose() == Pose.CROUCHING ? 1.5F : 1F)
			* super.getJumpPower();
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader level)
	{
		return this.wantsToHunt()
			? this.getHuntingBlockPathWeight(pos, level)
			: this.getHidingBlockPathWeight(pos, level);
	}
	
	////// Minecraft Events //////

	@Override
	protected void updateNoActionTime()
	{
		// for all MobEntities, idle time increments by +1/tick when a player isn't within 32 meters (and resets if a player is near)
		// for MonsterEntities, idle() (which is declared by MonsterEntity) increments idle time by an additional +2/tick while the monster is in daylight
		// we don't need the extra idle time for this monster, we want it to hang out underwater
		// so we override this method to do nothing
	}

	@Override
	public void tick()
	{
		super.tick();
		if (!this.level().isClientSide())	// do game logic on server
		{
			if (!this.dead) // make sure we only do these things if we didn't die during super.tick()
			{
				// slowly heal while in water
				if (this.isInWater() && this.level().getRandom().nextInt() % 20 == 0)
				{
					this.heal(1F);
				}
				
				// handle fullness
				if (this.fullness > 0)
				{
					this.decrementFullness();
				}
				else
				{
					this.setFullness(0);
				}
				
				// handle pose
				@Nullable LivingEntity target = this.getTarget();
				if ((this.onGround() && !this.jumping) || this.isInWater())
				{
					this.setDiscardFriction(false);
				}
				if (this.onGround() && !this.jumping && target == null)
				{
					if (this.getRandom().nextInt(100) == 0)
					{
						this.setPose(Pose.CROUCHING);
					}
				}
				else if (!this.onGround() || (this.getRandom().nextInt(100) == 0))
				{
					this.setPose(Pose.STANDING);
				}
				else
				{
					if (target != null)
					{
						double distSq = this.distanceToSqr(target);
						double attackReachSq = this.getBbWidth() * 2.0F * target.getBbWidth() * 2.0F + target.getBbWidth();
						if (distSq > attackReachSq && distSq < attackReachSq * 40D && this.getRandom().nextInt(20) == 0)
						{
							this.setPose(Pose.CROUCHING);
						}
					}
				}
			}
		}
	}

	// called when this entity kills another entity
	@Override
	public boolean killedEntity(ServerLevel level, LivingEntity killedEntity, DamageSource damageSource)
	{
		this.addFullness(Froglins.SERVERCONFIG.froglinFullnessFromKill().get());
		this.addEggs(1);
		return super.killedEntity(level, killedEntity, damageSource);
	}

	@Override
	public int getMaxFallDistance()
	{
		return super.getMaxFallDistance()*2;
	}

	@Override
	public void jumpFromGround()
	{
		super.jumpFromGround();
	}
	
	////// Syncing and Saving //////
	@Override
	public void addAdditionalSaveData(ValueOutput output)
	{
		super.addAdditionalSaveData(output);
		output.putInt(FULLNESS, this.fullness);
		output.putInt(EGGS, this.eggs);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(ValueInput input)
	{
		super.readAdditionalSaveData(input);
		this.fullness = input.getIntOr(FULLNESS, 0);
		this.eggs = input.getIntOr(EGGS, 0);
	}
	
	////// Froglin-Specific Status and Properties //////

	// note that nighttime spawning will use the hunting path weight
	// this needs to ensure that any block we would want to spawn in (e.g. water) >= 0
	public float getHuntingBlockPathWeight(BlockPos pos, LevelReader level)
	{
		// prefer highlands, prefer far away places
		return Math.min(0F, pos.getY()) * (float)pos.distSqr(this.blockPosition());
	}
	
	public float getHidingBlockPathWeight(BlockPos pos, LevelReader level)
	{
		int base = level.getMaxY()- pos.getY();	// prefer lowlands
		if (level.getFluidState(pos).is(FluidTags.WATER))	// greatly prefer water
		{
			return base * 10F;
		}
		else
		{
			return base;
		}
	}
	
	public boolean wantsToHide()
	{
		return (this.getTarget() == null || this.wantsToRunAway()) && !this.wantsToHunt();
	}
	
	public boolean wantsToRunAway()
	{
		return this.isLowLife()
			&& !this.isInWater();
	}

	public boolean wantsToHunt()
	{
		return this.isHungry() && this.doesWeatherAllowHunting() && !this.isLowLife();
	}
	
	// return true if the sun isn't out or it's raining
	public boolean doesWeatherAllowHunting()
	{
		return this.level().isDarkOutside() || 
			(this.level().isRaining() &&
				this.level().getBiome(this.blockPosition())
					.value()
					.getPrecipitationAt(this.blockPosition(), this.level().getSeaLevel()) == Precipitation.RAIN);
	}
	
	public boolean wantsToRetaliate()
	{
		return !this.isLowLife()
			|| !this.moveToWaterGoal.getIsRunning()
			|| this.isInWater();
	}
	
	public boolean isLowLife()
	{
		return this.getHealth() / this.getMaxHealth() < 0.3F;
	}
	
	public boolean isHungry()
	{
		return this.fullness <= 0;
	}

	public boolean laysPersistentEggs()
	{
		return Froglins.SERVERCONFIG.persistentFroglinsLayPersistentFroglinEggs().get() && this.isPersistenceRequired();
	}
	
	// need some tweaks to water movement to assist with turning, like drowned
	public static class FroglinMovementController extends MoveControl
	{

		private final FroglinEntity froglin;
		
		public FroglinMovementController(FroglinEntity froglin)
		{
			super(froglin);
			this.froglin = froglin;
		}

		@Override
		public void tick()
		{
			LivingEntity livingentity = this.froglin.getTarget();
			if (this.froglin.wantsToSwim() && this.froglin.isInWater())
			{
				if (livingentity != null && livingentity.getY() > this.froglin.getY())
				{
					this.froglin.setDeltaMovement(this.froglin.getDeltaMovement().add(0.0, 0.002, 0.0));
				}

				if (this.operation != MoveControl.Operation.MOVE_TO || this.froglin.getNavigation().isDone())
				{
					this.froglin.setSpeed(0.0F);
					return;
				}

				double d0 = this.wantedX - this.froglin.getX();
				double d1 = this.wantedY - this.froglin.getY();
				double d2 = this.wantedZ - this.froglin.getZ();
				double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
				d1 /= d3;
				float f = (float) (Mth.atan2(d2, d0) * 180.0F / (float) Math.PI) - 90.0F;
				this.froglin.setYRot(this.rotlerp(this.froglin.getYRot(), f, 90.0F));
				this.froglin.yBodyRot = this.froglin.getYRot();
				float f1 = (float) (this.speedModifier * this.froglin.getAttributeValue(Attributes.MOVEMENT_SPEED));
				float f2 = Mth.lerp(0.125F, this.froglin.getSpeed(), f1);
				this.froglin.setSpeed(f2);
				this.froglin.setDeltaMovement(this.froglin.getDeltaMovement().add(f2 * d0 * 0.005, f2 * d1 * 0.1, f2 * d2 * 0.005));
			}
			else
			{
				super.tick();
			}

		}
	}

	@Override
	public boolean isPushedByFluid()
	{
		return false;
	}
	
	protected boolean wantsToSwim()
	{
		@Nullable LivingEntity target = this.getTarget();
		return target != null && target.isInWater();
	}
	
	
	
    @Override
	public void setJumping(boolean jumping)
	{
    	if (jumping == true)
    	{
    		super.setJumping(jumping);
    	}
    	else
    	{
    		super.setJumping(jumping);
    	}
	}
    
	@Override
	public void setDeltaMovement(Vec3 vec3)
	{
    	if (!this.level().isClientSide())
    	{
    		super.setDeltaMovement(vec3);
    	}
    	else
    	{
    		super.setDeltaMovement(vec3);	
    	}
	}
    
	@Override
    protected void travelInWater(Vec3 movement, double effectiveGravity, boolean goingDown, double y) {
        if (this.isUnderWater() && this.wantsToSwim()) {
            this.moveRelative(0.05F, movement);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.getWaterSlowDown()));
        } else {
            super.travelInWater(movement, effectiveGravity, goingDown, y);
        }
	}

	@Override
	public void updateSwimming()
	{
		if (!this.level().isClientSide())
		{
			this.setSwimming(this.isEffectiveAi() && this.isUnderWater() && this.wantsToSwim());
		}
	}

	@Override
	public boolean isVisuallySwimming()
	{
		return this.isSwimming() && !this.isPassenger();
	}
	
	
	
	@Override
	protected float getFlyingSpeed()
	{
		if (this.shouldDiscardFriction())
		{
			return 1F;
		}
		return super.getFlyingSpeed() * 4F;
	}

	public boolean closeToNextPos()
	{
		Path path = this.getNavigation().getPath();
		if (path != null)
		{
			BlockPos blockpos = path.getTarget();
			if (blockpos != null)
			{
				double d0 = this.distanceToSqr(blockpos.getX(), blockpos.getY(), blockpos.getZ());
				if (d0 < 4.0)
				{
					return true;
				}
			}
		}

		return false;
	}
}
