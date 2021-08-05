package commoble.froglins.ai;

import java.util.EnumSet;
import java.util.Random;

import javax.annotation.Nullable;

import commoble.froglins.FroglinEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

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
			Vector3d target = this.tryFindWater();
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
	private Vector3d tryFindWater()
	{
		Random rand = this.froglin.getRandom();
		BlockPos froglinPos = this.froglin.blockPosition();

		for (int i = 0; i < 10; ++i)
		{
			BlockPos checkPos = froglinPos.offset(rand.nextInt(20) - 10, 2 - rand.nextInt(8), rand.nextInt(20) - 10);
			if (this.froglin.level.getBlockState(checkPos).is(Blocks.WATER))
			{
				return Vector3d.atBottomCenterOf(checkPos);
			}
		}

		return null;
	}
}
