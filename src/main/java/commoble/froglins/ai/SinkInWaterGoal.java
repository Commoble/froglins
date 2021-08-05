package commoble.froglins.ai;

import java.util.EnumSet;

import commoble.froglins.FroglinEggBlock;
import commoble.froglins.FroglinEntity;
import commoble.froglins.Froglins;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class SinkInWaterGoal extends Goal
{
	private final FroglinEntity froglin;
	
	public SinkInWaterGoal(FroglinEntity froglin)
	{
		this.froglin = froglin;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	// whether AI routine should start
	// shouldContinueExecuting delegates to this as well
	@Override
	public boolean canUse()
	{
		return this.froglin.isInWater() && this.froglin.wantsToHide();
	}

	// runs every tick while this routine is running
	@Override
	public void tick()
	{
		Level world = this.froglin.level;
		if (world != null && !world.isClientSide)
		{
			
			// handle digging
			if (world != null && (world.getGameTime() + this.froglin.hashCode()) % Froglins.INSTANCE.serverConfig.froglinDigFrequency.get() == 0)
			{
				BlockPos pos = this.froglin.blockPosition();
				if (world.isEmptyBlock(pos.above(2)) || world.isEmptyBlock(pos.above(3)))
				{
					int posX = pos.getX();
					int posY = pos.getY();
					int posZ = pos.getZ();
					Iterable<BlockPos> digPositions = BlockPos.betweenClosed(posX-1, posY-1, posZ-1, posX+1, posY-1, posZ+1);
					for (BlockPos checkPos : digPositions)
					{
						BlockPos aboveCheckPos = checkPos.above();
						boolean tryDig = (checkPos.getX() == pos.getX() && checkPos.getZ() == pos.getZ()) || world.random.nextInt(4) == 0;
						if (tryDig
							&& Froglins.DIGGABLE_TAG.contains(world.getBlockState(checkPos).getBlock())
							&& world.isWaterAt(aboveCheckPos)
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
				&& this.froglin.getRandom().nextInt(Froglins.INSTANCE.serverConfig.froglinEggFrequency.get()) == 0)
			{
				tryPlaceEgg(world, this.froglin);
			}
		}
		
	}
	
	protected static void tryPlaceEgg(Level world, FroglinEntity froglin)
	{
		BlockPos froglinPos = froglin.blockPosition();
		
		// check froglin pos first 
		
		FroglinEggBlock eggBlock = Froglins.INSTANCE.froglinEggBlock.get();
		
		if (eggBlock.isPositionValidAndInWater(world, froglinPos))
		{
			world.setBlockAndUpdate(froglinPos, eggBlock.defaultBlockState()
				.setValue(FroglinEggBlock.WATERLOGGED, true)
				.setValue(FroglinEggBlock.PERSISTANT, froglin.laysPersistantEggs()));
			froglin.data.addEggs(-1);
			return;
		}
		
		// otherwise, check random block nearby
		
		int xOff = -2 + world.random.nextInt(5);
		int yOff = -1 + world.random.nextInt(3);
		int zOff = -2 + world.random.nextInt(5);
		BlockPos checkPos = froglinPos.offset(xOff,yOff,zOff);
		
		if (eggBlock.isPositionValidAndInWater(world, checkPos))
		{
			world.setBlockAndUpdate(checkPos, eggBlock.defaultBlockState()
				.setValue(FroglinEggBlock.WATERLOGGED, true)
				.setValue(FroglinEggBlock.PERSISTANT, froglin.laysPersistantEggs()));
			froglin.data.addEggs(-1);
		}
	}

}
