package commoble.froglins;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.froglins.ai.JumpSometimesGoal;
import commoble.froglins.ai.MoveToWaterGoal;
import commoble.froglins.ai.PredicatedGoal;
import commoble.froglins.ai.SinkInWaterGoal;
import commoble.froglins.ai.SwimToTargetGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;

public class FroglinEntity extends MonsterEntity
{
	public static final String DATA = "FroglinData";
	
	public FroglinData data = new FroglinData();
	
	// make a reference to this so we can check if this is running or not
	// we can't set it here because registerGoals is called from MobEntity's constructor
	// i.e. *before* FroglinEntity's constructor
	private MoveToWaterGoal moveToWaterGoal;

	public FroglinEntity(EntityType<? extends FroglinEntity> type, World worldIn)
	{
		super(type, worldIn);
		this.moveController = new FroglinMovementController(this);
	}
	
	////// Entity Properties //////

	public static AttributeModifierMap.MutableAttribute createAttributes()
	{
//		return MonsterEntity.func_234295_eP_(); // standard attributes
		return MonsterEntity.func_234295_eP_()
			.createMutableAttribute(Attributes.FOLLOW_RANGE, 20.0D)	// zombies are 35, monster default is 16
			.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.3F)	// zombies are 0.23F, players are 0.7F?
			.createMutableAttribute(ForgeMod.SWIM_SPEED.get(), 1.5F)
			.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0D);
	}
	
	public static boolean canRandomlySpawn(EntityType<FroglinEntity> type, IServerWorld world, SpawnReason reason, BlockPos pos, Random rand)
	{
		int minY = world.getSeaLevel() - 5;
		int y = pos.getY();
		return world.getDifficulty() != Difficulty.PEACEFUL
			&& y >= minY
			&& MonsterEntity.isValidLightLevel(world, pos, rand)
			&& (reason == SpawnReason.SPAWNER || world.getBlockState(pos).getFluidState().isTagged(FluidTags.WATER));
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
			new SwimGoal(this),
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
		this.goalSelector.addGoal(7, new RandomWalkingGoal(this, 1D, 4)); // MOVE
		this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));	// LOOK
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));	// MOVE, LOOK
		
		// TODO good idea but make sure they don't interfere with other ai (particularly hiding)
//		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, OcelotEntity.class, 6.0F, 1.0D, 1.2D));
//		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, CatEntity.class, 6.0F, 1.0D, 1.2D));
		
		// TODO set calls for help?
		this.targetSelector.addGoal(2, new PredicatedGoal<>(this,
			new HurtByTargetGoal(this).setCallsForHelp(),
			FroglinEntity::wantsToRetaliate));
		this.targetSelector.addGoal(3, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, MobEntity.class, 20, false, false, entity -> Froglins.EDIBLE_FISH_TAG.contains(entity.getType())),
			FroglinEntity::wantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, MobEntity.class, 40, false, false, entity -> Froglins.EDIBLE_ANIMALS_TAG.contains(entity.getType())),
			FroglinEntity::wantsToHunt));
		this.targetSelector.addGoal(5, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 100, false, false, null),
			FroglinEntity::wantsToHunt));
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer)
	{
		return this.idleTime > this.data.getFullness() && super.canDespawn(distanceToClosestPlayer);
	}

	// same as players (zombies are -0.45, skeletons are -0.6)
	@Override
	public double getYOffset()
	{
		return -0.35D;
	}

	@Override
	public float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn)
	{
		// returns size * 0.85
		// for players, depends on pose
		return super.getStandingEyeHeight(poseIn, sizeIn);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ENTITY_ZOGLIN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENTITY_ZOGLIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_ZOGLIN_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(SoundEvents.ENTITY_ZOGLIN_STEP, 0.15F, 1.0F);
	}

	@Override
	public CreatureAttribute getCreatureAttribute()
	{
		return CreatureAttribute.WATER;
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
	public boolean isNotColliding(IWorldReader worldIn)
	{
		return worldIn.checkNoEntityCollision(this);
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
	protected float getJumpUpwardsMotion()
	{
		return (1F + (float)this.rand.nextGaussian()*0.5F)
			* (this.getPose() == Pose.CROUCHING ? 1.5F : 1F)
			* super.getJumpUpwardsMotion();
	}

	@Override
	public float getBlockPathWeight(BlockPos pos, IWorldReader world)
	{
		return this.wantsToHunt()
			? this.getHuntingBlockPathWeight(pos, world)
			: this.getHidingBlockPathWeight(pos, world);
	}
	
	////// Minecraft Events //////

	@Override
	protected void idle()
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
		if (!this.world.isRemote)	// do game logic on server
		{
			if (!this.dead) // make sure we only do these things if we didn't die during super.tick()
			{
				// slowly heal while in water
				if (this.isInWater() && this.world.getRandom().nextInt() % 20 == 0)
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
				if (this.isOnGround() && !this.isJumping && !this.moveController.isUpdating())
				{
					if (this.getRNG().nextInt(100) == 0)
					{
						this.setPose(Pose.CROUCHING);
					}
				}
				else if (!this.isOnGround() || this.getRNG().nextInt(20) == 0)
				{
					this.setPose(Pose.STANDING);
				}
				else
				{
					LivingEntity target = this.getAttackTarget();
					if (target != null)
					{
						double distSq = this.getDistanceSq(target);
						double attackReachSq = this.getWidth() * 2.0F * target.getWidth() * 2.0F + target.getWidth();
						if (distSq > attackReachSq && distSq < attackReachSq * attackReachSq && this.getRNG().nextInt(100) == 0)
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
	public void func_241847_a(ServerWorld world, LivingEntity killedEntity)
	{
		super.func_241847_a(world, killedEntity);
		this.data.addFullness(Froglins.INSTANCE.serverConfig.froglinFullnessFromKill.get());
		this.data.addEggs(1);
	}

	@Override
	public int getMaxFallHeight()
	{
		return super.getMaxFallHeight()*2;
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier)
	{
		return super.calculateFallDamage(distance*0.4F, damageMultiplier);
	}

	@Override
	public void jump()
	{
		super.jump();
	}
	
	////// Syncing and Saving //////
	@Override
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		FroglinData.CODEC.encodeStart(NBTDynamicOps.INSTANCE, this.data)
			.result()
			.ifPresent(dataTag -> compound.put(DATA, dataTag));
		;
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		FroglinData.CODEC.parse(NBTDynamicOps.INSTANCE, compound.get(DATA))
			.result()
			.ifPresent(readData -> this.data = readData);
	}
	
	////// Froglin-Specific Status and Properties //////

	// note that nighttime spawning will use the hunting path weight
	// this needs to ensure that any block we would want to spawn in (e.g. water) >= 0
	public float getHuntingBlockPathWeight(BlockPos pos, IWorldReader world)
	{
		// prefer highlands, prefer far away places
		return Math.min(0F, pos.getY()) * (float)pos.distanceSq(this.getPosition());
	}
	
	public float getHidingBlockPathWeight(BlockPos pos, IWorldReader world)
	{
		int base = world.getHeight() - pos.getY();	// prefer lowlands
		if (world.getFluidState(pos).isTagged(FluidTags.WATER))	// greatly prefer water
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
		return (this.getAttackTarget() == null || this.wantsToRunAway()) && !this.wantsToHunt();
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
		return !this.world.isDaytime() || 
			(this.world.isRaining() &&
				this.world.getBiome(this.getPosition())
				.getPrecipitation() == RainType.RAIN);
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
		return Froglins.INSTANCE.serverConfig.persistantFroglinsLayPersistantFroglinEggs.get() && this.isNoDespawnRequired();
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
	public static class FroglinMovementController extends MovementController
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
			LivingEntity target = this.froglin.getAttackTarget();
			Vector3d velocity = this.froglin.getMotion();
			if (target != null)
			{
				if (this.froglin.isInWater())
				{
					if (target.isInWater())
					{
						if (this.froglin.getNavigator().noPath())
						{
							this.froglin.setAIMoveSpeed(0.0F);
							return;
						}

						double dx = this.posX - this.froglin.getPosX();
						double dy = this.posY - this.froglin.getPosY();
						double dz = this.posZ - this.froglin.getPosZ();
						double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
						dy = dy / distance;
						float yawDegrees = (float) (MathHelper.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F;
						this.froglin.rotationYaw = this.limitAngle(this.froglin.rotationYaw, yawDegrees, 90.0F);
						this.froglin.renderYawOffset = this.froglin.rotationYaw;
					}
				}
				else if (!this.froglin.isOnGround())
				{
					if (velocity.x != 0.0D && velocity.z != 0.0D)
					{
						this.froglin.setMotion(velocity.x + 0.01D, velocity.y, velocity.z + 0.01D);
					}
					else
					{
						Vector3d offset = target.getPositionVec().subtract(this.froglin.getPositionVec()).normalize();
						this.froglin.setMotion(velocity.x + 0.01D * offset.x, velocity.y, velocity.z + 0.01D * offset.z);
					}
				}
			}
			super.tick();

		}
	}
}
