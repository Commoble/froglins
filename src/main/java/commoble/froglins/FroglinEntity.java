package commoble.froglins;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.froglins.ai.JumpSometimesGoal;
import commoble.froglins.ai.MoveToWaterGoal;
import commoble.froglins.ai.PredicatedGoal;
import commoble.froglins.ai.SinkInWaterGoal;
import commoble.froglins.ai.SwimToTargetGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeMod;

public class FroglinEntity extends Monster
{
	public static final String DATA = "FroglinData";
	
	public FroglinData data = new FroglinData();
	
	// make a reference to this so we can check if this is running or not
	// we can't set it here because registerGoals is called from MobEntity's constructor
	// i.e. *before* FroglinEntity's constructor
	private MoveToWaterGoal moveToWaterGoal;

	public FroglinEntity(EntityType<? extends FroglinEntity> type, Level worldIn)
	{
		super(type, worldIn);
		this.moveControl = new FroglinMovementController(this);
	}
	
	////// Entity Properties //////

	public static AttributeSupplier.Builder createAttributes()
	{
//		return MonsterEntity.createMonsterAttributes(); // standard attributes
		return Monster.createMonsterAttributes()
			.add(Attributes.FOLLOW_RANGE, 20.0D)	// zombies are 35, monster default is 16
			.add(Attributes.MOVEMENT_SPEED, 0.3F)	// zombies are 0.23F, players are 0.7F?
			.add(ForgeMod.SWIM_SPEED.get(), 1.5F)
			.add(Attributes.ATTACK_DAMAGE, 4.0D);
	}
	
	public static boolean canRandomlySpawn(EntityType<FroglinEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, Random rand)
	{
		int minY = world.getSeaLevel() - 5;
		int y = pos.getY();
		return world.getDifficulty() != Difficulty.PEACEFUL
			&& y >= minY
			&& Monster.isDarkEnoughToSpawn(world, pos, rand)
			&& (reason == MobSpawnType.SPAWNER || world.getBlockState(pos).getFluidState().is(FluidTags.WATER));
	}

	@Override
	protected void registerGoals()
	{
		this.moveToWaterGoal = new MoveToWaterGoal(this, 1.2D);
		
		this.goalSelector.addGoal(1, new SinkInWaterGoal(this)); // MOVE, JUMP
		this.goalSelector.addGoal(2, this.moveToWaterGoal);	// MOVE
		this.goalSelector.addGoal(2, new SwimToTargetGoal(this));	// JUMP
//		this.goalSelector.addGoal(3, new SwimGoal(this));
		this.goalSelector.addGoal(3, new PredicatedGoal<>(this,
			new FloatGoal(this),
			FroglinEntity::wantsToHunt));	// JUMP
		this.goalSelector.addGoal(3, new PredicatedGoal<>(this,
			new PanicGoal(this, 2D),
			FroglinEntity::wantsToRunAway));	// MOVE
		this.goalSelector.addGoal(4, new PredicatedGoal<>(this,
			new LeapAtTargetGoal(this, 1.0F),
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
			new NearestAttackableTargetGoal<>(this, Mob.class, 20, false, false, entity -> Froglins.EDIBLE_FISH_TAG.contains(entity.getType())),
			FroglinEntity::wantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, Mob.class, 40, false, false, entity -> Froglins.EDIBLE_ANIMALS_TAG.contains(entity.getType())),
			FroglinEntity::wantsToHunt));
		this.targetSelector.addGoal(5, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, Player.class, 100, false, false, null),
			FroglinEntity::wantsToHunt));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer)
	{
		return this.noActionTime > this.data.getFullness() && super.removeWhenFarAway(distanceToClosestPlayer);
	}

	// same as players (zombies are -0.45, skeletons are -0.6)
	@Override
	public double getMyRidingOffset()
	{
		return -0.35D;
	}

	@Override
	public float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn)
	{
		// returns size * 0.85
		// for players, depends on pose
		return super.getStandingEyeHeight(poseIn, sizeIn);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ZOGLIN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ZOGLIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ZOGLIN_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(SoundEvents.ZOGLIN_STEP, 0.15F, 1.0F);
	}

	@Override
	public MobType getMobType()
	{
		return MobType.WATER;
	}

	@Override
	public boolean canBreatheUnderwater()
	{
		return true;
	}

	// called during random spawn attempts, after the entity instance is created, but before it is canonically added to the world
	// normally this returns false if the entity is in water, or if the entity is touching another entity
	// water mobs must override this to skip the water check
	@Override
	public boolean checkSpawnObstruction(LevelReader worldIn)
	{
		return worldIn.isUnobstructed(this);
	}

	// affects how fast you stop moving after you stop moving (lower numbers = stop moving sooner)
	@Override
	protected float getWaterSlowDown()
	{
//		return super.getWaterSlowDown();
//		return 0.98F; // same as polar bears, highest value in vanilla
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
	public float getWalkTargetValue(BlockPos pos, LevelReader world)
	{
		return this.wantsToHunt()
			? this.getHuntingBlockPathWeight(pos, world)
			: this.getHidingBlockPathWeight(pos, world);
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
		if (!this.level.isClientSide)	// do game logic on server
		{
			if (!this.dead) // make sure we only do these things if we didn't die during super.tick()
			{
				// slowly heal while in water
				if (this.isInWater() && this.level.getRandom().nextInt() % 20 == 0)
				{
					this.heal(1F);
				}
				
				// handle fullness
				if (this.data.getFullness() > 0)
				{
					this.data.decrementFullness();
				}
				else
				{
					this.data.setFullness(0);
				}
				
				// handle pose
				if (this.isOnGround() && !this.jumping && !this.moveControl.hasWanted())
				{
					if (this.getRandom().nextInt(100) == 0)
					{
						this.setPose(Pose.CROUCHING);
					}
				}
				else if (!this.isOnGround() || this.getRandom().nextInt(20) == 0)
				{
					this.setPose(Pose.STANDING);
				}
				else
				{
					LivingEntity target = this.getTarget();
					if (target != null)
					{
						double distSq = this.distanceToSqr(target);
						double attackReachSq = this.getBbWidth() * 2.0F * target.getBbWidth() * 2.0F + target.getBbWidth();
						if (distSq > attackReachSq && distSq < attackReachSq * attackReachSq && this.getRandom().nextInt(100) == 0)
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
	public void killed(ServerLevel world, LivingEntity killedEntity)
	{
		super.killed(world, killedEntity);
		this.data.addFullness(Froglins.INSTANCE.serverConfig.froglinFullnessFromKill.get());
		this.data.addEggs(1);
	}

	@Override
	public int getMaxFallDistance()
	{
		return super.getMaxFallDistance()*2;
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier)
	{
		return super.calculateFallDamage(distance*0.4F, damageMultiplier);
	}

	@Override
	public void jumpFromGround()
	{
		super.jumpFromGround();
	}
	
	////// Syncing and Saving //////
	@Override
	public void addAdditionalSaveData(CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		FroglinData.CODEC.encodeStart(NbtOps.INSTANCE, this.data)
			.result()
			.ifPresent(dataTag -> compound.put(DATA, dataTag));
		;
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		FroglinData.CODEC.parse(NbtOps.INSTANCE, compound.get(DATA))
			.result()
			.ifPresent(readData -> this.data = readData);
	}
	
	////// Froglin-Specific Status and Properties //////

	// note that nighttime spawning will use the hunting path weight
	// this needs to ensure that any block we would want to spawn in (e.g. water) >= 0
	public float getHuntingBlockPathWeight(BlockPos pos, LevelReader world)
	{
		// prefer highlands, prefer far away places
		return Math.min(0F, pos.getY()) * (float)pos.distSqr(this.blockPosition());
	}
	
	public float getHidingBlockPathWeight(BlockPos pos, LevelReader world)
	{
		int base = world.getMaxBuildHeight() - pos.getY();	// prefer lowlands
		if (world.getFluidState(pos).is(FluidTags.WATER))	// greatly prefer water
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
		// !world.isDayTime evaluates true when we are in a world with no daylight cycle
		// world.isNightTime evaluates false in such worlds
		return this.isHungry() && this.doesWeatherAllowHunting() && !this.isLowLife();
	}
	
	// return true if the sun isn't out or it's raining
	public boolean doesWeatherAllowHunting()
	{
		return !this.level.isDay() || 
			(this.level.isRaining() &&
				this.level.getBiome(this.blockPosition())
				.getPrecipitation() == Precipitation.RAIN);
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
		return this.data.getFullness() <= 0;
	}

	public boolean laysPersistantEggs()
	{
		return Froglins.INSTANCE.serverConfig.persistentFroglinsLayPersistentFroglinEggs.get() && this.isPersistenceRequired();
	}
	
	public static class FroglinData
	{
		public static final FroglinData EMPTY = new FroglinData();
		
		public static final Codec<FroglinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.INT.optionalFieldOf("fullness", 0).forGetter(FroglinData::getFullness),
					Codec.INT.optionalFieldOf("eggs", 0).forGetter(FroglinData::getEggs)
				).apply(instance, FroglinData::new));
		
		private int fullness = 0;
		public int getFullness() { return this.fullness; }
		public void setFullness(int value) { this.fullness = value; }
		public void addFullness(int value) { this.fullness += value; }
		public void decrementFullness() { --this.fullness; }
		
		private int eggs = 0;
		public int getEggs() { return this.eggs; }
		public void setEggs(int value) { this.eggs = value; }
		public void addEggs(int value) { this.eggs += value; }
		
		public FroglinData()
		{
			this(0,0);
		}
		
		public FroglinData(int fullness, int eggs)
		{
			this.fullness = fullness;
			this.eggs = eggs;
		}
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
			LivingEntity target = this.froglin.getTarget();
			Vec3 velocity = this.froglin.getDeltaMovement();
			if (target != null)
			{
				if (this.froglin.isInWater())
				{
					if (target.isInWater())
					{
						if (this.froglin.getNavigation().isDone())
						{
							this.froglin.setSpeed(0.0F);
							return;
						}

						double dx = this.wantedX - this.froglin.getX();
						double dy = this.wantedY - this.froglin.getY();
						double dz = this.wantedZ - this.froglin.getZ();
						double distance = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
						dy = dy / distance;
						float yawDegrees = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
						this.froglin.setYRot(this.rotlerp(this.froglin.getYRot(), yawDegrees, 90.0F));
						this.froglin.yBodyRot = this.froglin.getYRot();
					}
				}
				else if (!this.froglin.isOnGround())
				{
					if (velocity.x != 0.0D && velocity.z != 0.0D)
					{
						this.froglin.setDeltaMovement(velocity.x + 0.01D, velocity.y, velocity.z + 0.01D);
					}
					else
					{
						Vec3 offset = target.position().subtract(this.froglin.position()).normalize();
						this.froglin.setDeltaMovement(velocity.x + 0.01D * offset.x, velocity.y, velocity.z + 0.01D * offset.z);
					}
				}
			}
			super.tick();

		}
	}
}
