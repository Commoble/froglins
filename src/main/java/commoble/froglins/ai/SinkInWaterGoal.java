package commoble.froglins.ai;

import java.util.EnumSet;

import commoble.froglins.FroglinEntity;
import commoble.froglins.Froglins;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SinkInWaterGoal extends Goal
{
	private final FroglinEntity froglin;
	
	public SinkInWaterGoal(FroglinEntity froglin)
	{
		this.froglin = froglin;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	// whether AI routine should start
	// shouldContinueExecuting delegates to this as well
	@Override
	public boolean shouldExecute()
	{
		return this.froglin.isInWater() && this.froglin.wantsToHide();
	}

	// runs every tick while this routine is running
	@Override
	public void tick()
	{
		World world = this.froglin.world;
		if (world != null && (world.getGameTime() + this.froglin.hashCode()) % Froglins.INSTANCE.serverConfig.froglinDigFrequency.get() == 0)
		{
			BlockPos pos = this.froglin.getPosition();
			if (world.isAirBlock(pos.up(2)) || world.isAirBlock(pos.up(3)))
			{
				int posX = pos.getX();
				int posY = pos.getY();
				int posZ = pos.getZ();
				Iterable<BlockPos> digPositions = BlockPos.getAllInBoxMutable(posX-1, posY-1, posZ-1, posX+1, posY-1, posZ+1);
				for (BlockPos checkPos : digPositions)
				{
					boolean tryDig = (checkPos.getX() == pos.getX() && checkPos.getZ() == pos.getZ()) || world.rand.nextInt(4) == 0;
					if (tryDig && Froglins.DIGGABLE_TAG.contains(world.getBlockState(checkPos).getBlock()))
					{
						world.removeBlock(checkPos, false);
					}
				}
			}
		}
	}

}
