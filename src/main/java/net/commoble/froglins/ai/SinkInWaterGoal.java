package net.commoble.froglins.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.commoble.froglins.FroglinEggBlock;
import net.commoble.froglins.FroglinEntity;
import net.commoble.froglins.Froglins;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
		Level level = this.froglin.level();
		if (level != null && !level.isClientSide())
		{
			
			// handle digging
			if (level != null && (level.getGameTime() + this.froglin.hashCode()) % Froglins.SERVERCONFIG.froglinDigFrequency().get() == 0)
			{
				BlockPos pos = this.froglin.blockPosition();
				if (level.isEmptyBlock(pos.above(2)) || level.isEmptyBlock(pos.above(3)))
				{
					int posX = pos.getX();
					int posY = pos.getY();
					int posZ = pos.getZ();
					Iterable<BlockPos> digPositions = BlockPos.betweenClosed(posX-1, posY-1, posZ-1, posX+1, posY-1, posZ+1);
					for (BlockPos checkPos : digPositions)
					{
						BlockPos aboveCheckPos = checkPos.above();
						boolean tryDig = (checkPos.getX() == pos.getX() && checkPos.getZ() == pos.getZ()) || level.random.nextInt(4) == 0;
						if (tryDig
							&& level.getBlockState(checkPos).is(Froglins.DIGGABLE_BLOCKS_TAG)
							&& level.isWaterAt(aboveCheckPos)
							&& level.getBlockState(aboveCheckPos).canBeReplaced())
						{
							level.removeBlock(checkPos, false);
						}
					}
				}
			}
			
			// handle eggs
			int eggs = this.froglin.getEggs();
			if (eggs > 0
				&& this.froglin.getPose() == Pose.CROUCHING
				&& this.froglin.getRandom().nextInt(Froglins.SERVERCONFIG.froglinEggFrequency().get()) == 0)
			{
				tryPlaceEgg(level, this.froglin);
			}
		}
		
	}
	
	protected static void tryPlaceEgg(Level level, FroglinEntity froglin)
	{
		BlockPos froglinPos = froglin.blockPosition();
		
		// check froglin pos first 
		
		FroglinEggBlock eggBlock = Froglins.FROGLIN_EGG_BLOCK.get();
		
		if (eggBlock.isPositionValidAndInWater(level, froglinPos))
		{
			level.setBlock(froglinPos, eggBlock.defaultBlockState()
				.setValue(FroglinEggBlock.WATERLOGGED, true)
				.setValue(FroglinEggBlock.PERSISTENT, froglin.laysPersistentEggs()),
				11);
			froglin.addEggs(-1);
			return;
		}
		
		// otherwise, check random block nearby
		
		int xOff = -2 + level.random.nextInt(5);
		int yOff = -1 + level.random.nextInt(3);
		int zOff = -2 + level.random.nextInt(5);
		BlockPos checkPos = froglinPos.offset(xOff,yOff,zOff);
		
		if (eggBlock.isPositionValidAndInWater(level, checkPos))
		{
			level.setBlock(checkPos, eggBlock.defaultBlockState()
				.setValue(FroglinEggBlock.WATERLOGGED, true)
				.setValue(FroglinEggBlock.PERSISTENT, froglin.laysPersistentEggs()),
				11);
			froglin.addEggs(-1);
		}
	}

}
