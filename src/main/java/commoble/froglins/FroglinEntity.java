package commoble.froglins;

import commoble.froglins.ai.MoveToWaterGoal;
import commoble.froglins.ai.PredicatedGoal;
import commoble.froglins.ai.SinkInWaterGoal;
import commoble.froglins.ai.SwimToTargetGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.fish.AbstractGroupFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class FroglinEntity extends MonsterEntity
{

	public FroglinEntity(EntityType<? extends FroglinEntity> type, World worldIn)
	{
		super(type, worldIn);
	}

	public static AttributeModifierMap.MutableAttribute createAttributes()
	{
//		return MonsterEntity.func_234295_eP_(); // standard attributes
		return MonsterEntity.func_234295_eP_()
			.createMutableAttribute(Attributes.FOLLOW_RANGE, 20.0D)	// zombies are 35, monster default is 16
			.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.23F)	// zombies are 0.23F, players are 0.7F?
			.createMutableAttribute(ForgeMod.SWIM_SPEED.get(), 1.3F)
			.createMutableAttribute(Attributes.ATTACK_DAMAGE, 3.0D);
	}

	@Override
	protected void registerGoals()
	{
		this.goalSelector.addGoal(1, new SinkInWaterGoal(this)); // MOVE, JUMP
		this.goalSelector.addGoal(2, new SwimToTargetGoal(this));	// JUMP
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1D, false));	// MOVE, LOOK
		this.goalSelector.addGoal(6, new MoveToWaterGoal(this, 1.2D));
		this.goalSelector.addGoal(7, new RandomWalkingGoal(this, 1D, 4)); // MOVE
		this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));	// LOOK
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));	// MOVE, LOOK
		
		// TODO good idea but make sure they don't interfere with other ai (particularly hiding)
//		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, OcelotEntity.class, 6.0F, 1.0D, 1.2D));
//		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, CatEntity.class, 6.0F, 1.0D, 1.2D));
		
		// TODO set calls for help?
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, AbstractGroupFishEntity.class, true),
			FroglinEntity::getWantsToHunt));
		this.targetSelector.addGoal(3, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, SquidEntity.class, true),
			FroglinEntity::getWantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, ChickenEntity.class, true),
			FroglinEntity::getWantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, CowEntity.class, true),
			FroglinEntity::getWantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, PigEntity.class, true),
			FroglinEntity::getWantsToHunt));
		this.targetSelector.addGoal(4, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, SheepEntity.class, true),
			FroglinEntity::getWantsToHunt));
		this.targetSelector.addGoal(5, new PredicatedGoal<>(this,
			new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true),
			FroglinEntity::getWantsToHunt));
		
		
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer)
	{
		return this.idleTime > Froglins.INSTANCE.serverConfig.froglinDespawnDelay.get() && super.canDespawn(distanceToClosestPlayer);
	}

	@Override
	protected void idle()
	{
		// for all MobEntities, idle time increments by +1/tick when a player isn't within 32 meters (and resets if a player is near)
		// for MonsterEntities, idle() increments idle time by an additional +2/tick while the monster is in daylight
		// we don't need the extra idle time for this monster, we want it to hang out underwater
		// so we override this method to do nothing
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

	// affects how fast you stop moving after you stop moving (lower numbers = stop moving sooner)
	@Override
	protected float getWaterSlowDown()
	{
//		return super.getWaterSlowDown();
//		return 0.98F; // same as polar bears, highest value in vanilla
		return 0.96F;	// same as skeleton horses (why do skeleton horses have high hydrodynamics?)
	}


	@Override
	public float getBlockPathWeight(BlockPos pos, IWorldReader world)
	{
		return this.getWantsToHunt()
			? this.getHuntingBlockPathWeight(pos, world)
			: this.getHidingBlockPathWeight(pos, world);
	}

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
		return this.getAttackTarget() == null && !this.getWantsToHunt();
	}

	public boolean getWantsToHunt()
	{
		// !world.isDayTime evaluates true when we are in a world with no daylight cycle
		// world.isNightTime evaluates false in such worlds
		return !this.world.isDaytime() && !this.isLowLife();
	}
	
	public boolean isLowLife()
	{
		return this.getHealth() / this.getMaxHealth() < 0.3F;
	}

	@Override
	public void tick()
	{
		super.tick();
		if (!this.world.isRemote && !this.dead && this.isInWater() && this.world.getRandom().nextInt() % 20 == 0)
		{
			this.heal(1F);
		}
	}
}
