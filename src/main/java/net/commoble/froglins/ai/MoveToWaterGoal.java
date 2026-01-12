package net.commoble.froglins.ai;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.commoble.froglins.FroglinEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

// this is mostly the private goal from DrownedEntity with some adjustments
public class MoveToWaterGoal extends Goal
{
	private final FroglinEntity froglin;
	private final double speedMultiplier;
	private double x;
	private double y;
	private double z;
	
	private boolean isRunning = false;
	public boolean getIsRunning() { return this.isRunning; }
	
	public MoveToWaterGoal(FroglinEntity froglin, double speedMultiplier)
	{
		this.froglin = froglin;
		this.speedMultiplier = speedMultiplier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse()
	{
		if (this.froglin.wantsToHide() && !this.froglin.isInWater())
		{
			Vec3 target = this.tryFindWater();
			if (target == null)
			{
				return false;
			}
			else
			{
				this.x = target.x();
				this.y = target.y();
				this.z = target.z();
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean canContinueToUse()
	{
		return !this.froglin.getNavigation().isDone();
	}
	
	@Override
	public void start()
	{
		this.isRunning = true;
		this.froglin.getNavigation().moveTo(this.x, this.y, this.z, this.speedMultiplier);
	}
	
	@Override
	public void stop()
	{
		this.isRunning = false;
	}

	@Nullable
	private Vec3 tryFindWater()
	{
		RandomSource rand = this.froglin.getRandom();
		BlockPos froglinPos = this.froglin.blockPosition();

		for (int i = 0; i < 10; ++i)
		{
			BlockPos checkPos = froglinPos.offset(rand.nextInt(20) - 10, 2 - rand.nextInt(8), rand.nextInt(20) - 10);
			if (this.froglin.level().getBlockState(checkPos).is(Blocks.WATER))
			{
				return Vec3.atBottomCenterOf(checkPos);
			}
		}

		return null;
	}
}
