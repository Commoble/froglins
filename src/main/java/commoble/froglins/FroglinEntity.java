package commoble.froglins;

import commoble.froglins.ai.SinkInWaterGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class FroglinEntity extends MonsterEntity
{

	public FroglinEntity(EntityType<? extends FroglinEntity> type, World worldIn)
	{
		super(type, worldIn);
	}

	public static AttributeModifierMap.MutableAttribute createAttributes()
	{
		return MonsterEntity.func_234295_eP_(); // standard attributes
	}

	@Override
	protected void registerGoals()
	{
		this.goalSelector.addGoal(1, new SinkInWaterGoal(this));
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

	@Override
	public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn)
	{
		return super.getBlockPathWeight(pos, worldIn);
	}
	
	public boolean wantsToHide()
	{
		return true;
	}

}
