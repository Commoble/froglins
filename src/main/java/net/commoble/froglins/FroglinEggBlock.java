package net.commoble.froglins;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FroglinEggBlock extends Block implements SimpleWaterloggedBlock, BonemealableBlock
{
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty PERSISTENT = BooleanProperty.create("persistent");
	public static final IntegerProperty HATCH_PROGRESS = BlockStateProperties.AGE_15;
	
	public static final VoxelShape SHAPE = Block.box(2D, 0D, 2D, 14D, 6D, 14D);

	public FroglinEggBlock(Properties properties)
	{
		super(properties);
		BlockState defaultState = this.stateDefinition.any()
			.setValue(WATERLOGGED, false)
			.setValue(PERSISTENT, false)
			.setValue(HATCH_PROGRESS, 0);
		this.registerDefaultState(defaultState);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED, PERSISTENT, HATCH_PROGRESS);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockPos placePos = context.getClickedPos();
		FluidState fluidState = context.getLevel().getFluidState(placePos);
		BlockState stateToPlace = super.getStateForPlacement(context)
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
			.setValue(PERSISTENT, Froglins.SERVERCONFIG.playersPlacePersistentFroglinEggs().get());
		
		if (this.canSurvive(stateToPlace, context.getLevel(), placePos))
		{
			return stateToPlace;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
		BlockPos belowPos = pos.below();
		return this.isValidGround(level.getBlockState(belowPos), level, belowPos);
	}

	protected boolean isValidGround(BlockState belowState, BlockGetter level, BlockPos belowPos)
	{
		return belowState.isSolidRender();
	}
	
	public boolean isPositionValidAndInWater(LevelReader level, BlockPos pos)
	{
		return level.isWaterAt(pos)
			&& level.getBlockState(pos).canBeReplaced()
			&& this.canSurvive(this.defaultBlockState(), level, pos);
	}

	@Override
	public BlockState updateShape(BlockState thisState, LevelReader level, ScheduledTickAccess ticker, BlockPos thisPos, Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState, RandomSource random)
	{
		if (!thisState.canSurvive(level, thisPos))
		{
			return Blocks.AIR.defaultBlockState();
		}
		else
		{
			if (thisState.getValue(WATERLOGGED))
			{
				ticker.scheduleTick(thisPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
			}
			return super.updateShape(thisState, level, ticker, thisPos, directionToNeighbor, neighborPos, neighborState, random);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		// make sure this block is correct
		if (state.is(Froglins.FROGLIN_EGG_BLOCK.get()))
		{
			int progress = state.getValue(HATCH_PROGRESS);
			if (this.canGrowProgressAtPosition(level, pos, state))
			{
				BlockPos abovePos = pos.above();
				if (progress < 15) // not hatchable yet
				{
					level.setBlockAndUpdate(pos, state.setValue(HATCH_PROGRESS, progress+1));
				}
				else if (level.isDarkOutside()
					&& level.getBlockState(abovePos).getCollisionShape(level, abovePos).isEmpty()
					&& this.areEnoughPlayersNearToHatch(level, pos, state))
				{
					this.hatch(level, pos, state, random);
				}
			}
		}
	}
	
	protected void hatch(ServerLevel level, BlockPos pos, BlockState state, RandomSource random)
	{
		boolean persistant = state.getValue(PERSISTENT);
		level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());


       FroglinEntity froglin = Froglins.FROGLIN.get().create(level, EntitySpawnReason.TRIGGERED);
       froglin.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360F, 0.0F);
       if (persistant)
       {
    	   froglin.setPersistenceRequired();
       }
       level.addFreshEntity(froglin);
	}

	public boolean canGrowProgressAtPosition(LevelReader level, BlockPos pos, BlockState state)
	{
		return level.getFluidState(pos).getType() == Fluids.WATER
			&& this.canSurvive(state, level, pos);
	}
	
	public boolean areEnoughPlayersNearToHatch(EntityGetter level, BlockPos pos, BlockState state)
	{
		return state.getValue(PERSISTENT)
			// the boolean arg at the end of getClosestPlayer *ignores* creative players if true
			|| level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), MobCategory.MONSTER.getNoDespawnDistance(), false) != null;
	}
	
	@Override
	@Deprecated
	public FluidState getFluidState(BlockState state)
	{
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	// called first, on both server/client
	// if this returns true, bonemeal stack will shrink and animation will play
	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state)
	{
		return this.canDefinitelyUseBonemeal(level, pos, state);
	}

	// called immediately after canGrow, only on servers
	// if this returns true, grow will be called
	@Override
	public boolean isBonemealSuccess(Level level, RandomSource rand, BlockPos pos, BlockState state)
	{
		return this.canDefinitelyUseBonemeal(level, pos, state);
	}
	
	protected boolean canDefinitelyUseBonemeal(LevelReader level, BlockPos pos, BlockState state)
	{
		// make sure this block is correct
		if (state.is(Froglins.FROGLIN_EGG_BLOCK.get()))
		{
			BlockPos abovePos = pos.above();
			return this.canGrowProgressAtPosition(level, pos, state)
				&& level.getBlockState(abovePos).getCollisionShape(level, abovePos).isEmpty();
		}
		else
		{
			return false;
		}
	}

	// called when canUseBonemeal returns true on server
	@Override
	public void performBonemeal(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state)
	{
		int progress = state.getValue(HATCH_PROGRESS);
		int progressRemaining = 15 - progress;
		if (progressRemaining > 0) // not time to hatch yet
		{
			// let's say we are at stage 14
			// progressRemaining = 1
			// we can advance by [0 or 1] stages
			// so we need to call nextInt(2)
			// which is progressRemaining+1
			int progressIncrease = rand.nextInt(progressRemaining+1);
			level.setBlockAndUpdate(pos, state.setValue(HATCH_PROGRESS, progress + progressIncrease));
		}
		else
		{
			this.hatch(level, pos, state, rand);
		}
		
	}
	
	

}
