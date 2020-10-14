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
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean shouldExecute()
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
				this.x = target.getX();
				this.y = target.getY();
				this.z = target.getZ();
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return !this.froglin.getNavigator().noPath();
	}
	
	@Override
	public void startExecuting()
	{
		this.isRunning = true;
		this.froglin.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speedMultiplier);
	}
	
	@Override
	public void resetTask()
	{
		this.isRunning = false;
	}

	@Nullable
	private Vector3d tryFindWater()
	{
		Random rand = this.froglin.getRNG();
		BlockPos froglinPos = this.froglin.getPosition();

		for (int i = 0; i < 10; ++i)
		{
			BlockPos checkPos = froglinPos.add(rand.nextInt(20) - 10, 2 - rand.nextInt(8), rand.nextInt(20) - 10);
			if (this.froglin.world.getBlockState(checkPos).isIn(Blocks.WATER))
			{
				return Vector3d.copyCenteredHorizontally(checkPos);
			}
		}

		return null;
	}
}
