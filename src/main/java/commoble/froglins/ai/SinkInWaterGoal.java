package commoble.froglins.ai;

import java.util.EnumSet;

import commoble.froglins.FroglinEggBlock;
import commoble.froglins.FroglinEntity;
import commoble.froglins.Froglins;
import net.minecraft.entity.Pose;
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
		if (world != null && !world.isRemote)
		{
			
			// handle digging
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
						BlockPos aboveCheckPos = checkPos.up();
						boolean tryDig = (checkPos.getX() == pos.getX() && checkPos.getZ() == pos.getZ()) || world.rand.nextInt(4) == 0;
						if (tryDig
							&& Froglins.DIGGABLE_TAG.contains(world.getBlockState(checkPos).getBlock())
							&& world.hasWater(aboveCheckPos)
							&& world.getBlockState(aboveCheckPos).getMaterial().isReplaceable())
						{
							world.removeBlock(checkPos, false);
						}
					}
				}
			}
			
			// handle eggs
			int eggs = this.froglin.data.getEggs();
			if (eggs > 0
				&& this.froglin.getPose() == Pose.CROUCHING
				&& this.froglin.getRNG().nextInt(Froglins.INSTANCE.serverConfig.froglinEggFrequency.get()) == 0)
			{
				tryPlaceEgg(world, this.froglin);
			}
		}
		
	}
	
	protected static void tryPlaceEgg(World world, FroglinEntity froglin)
	{
		BlockPos froglinPos = froglin.getPosition();
		
		// check froglin pos first 
		
		FroglinEggBlock eggBlock = Froglins.INSTANCE.froglinEggBlock.get();
		
		if (eggBlock.isPositionValidAndInWater(world, froglinPos))
		{
			world.setBlockState(froglinPos, eggBlock.getDefaultState()
				.with(FroglinEggBlock.WATERLOGGED, true)
				.with(FroglinEggBlock.PERSISTANT, froglin.laysPersistantEggs()));
			froglin.data.addEggs(-1);
			return;
		}
		
		// otherwise, check random block nearby
		
		int xOff = -2 + world.rand.nextInt(5);
		int yOff = -1 + world.rand.nextInt(3);
		int zOff = -2 + world.rand.nextInt(5);
		BlockPos checkPos = froglinPos.add(xOff,yOff,zOff);
		
		if (eggBlock.isPositionValidAndInWater(world, checkPos))
		{
			world.setBlockState(checkPos, eggBlock.getDefaultState()
				.with(FroglinEggBlock.WATERLOGGED, true)
				.with(FroglinEggBlock.PERSISTANT, froglin.laysPersistantEggs()));
			froglin.data.addEggs(-1);
		}
	}

}
