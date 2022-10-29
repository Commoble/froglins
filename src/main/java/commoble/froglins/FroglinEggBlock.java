package commoble.froglins;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
			.setValue(PERSISTENT, Froglins.INSTANCE.serverConfig.playersPlacePersistentFroglinEggs.get());
		
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
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		BlockPos belowPos = pos.below();
		return this.isValidGround(worldIn.getBlockState(belowPos), worldIn, belowPos);
	}

	protected boolean isValidGround(BlockState belowState, BlockGetter worldIn, BlockPos belowPos)
	{
		return belowState.isSolidRender(worldIn, belowPos);
	}
	
	public boolean isPositionValidAndInWater(LevelReader world, BlockPos pos)
	{
		return world.isWaterAt(pos)
			&& world.getBlockState(pos).getMaterial().isReplaceable()
			&& this.canSurvive(this.defaultBlockState(), world, pos);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if (!stateIn.canSurvive(worldIn, currentPos))
		{
			return Blocks.AIR.defaultBlockState();
		}
		else
		{
			if (stateIn.getValue(WATERLOGGED))
			{
				worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
			}

			@SuppressWarnings("deprecation")
			BlockState result = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
			return result;
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		// make sure this block is correct
		if (state.is(Froglins.INSTANCE.froglinEggBlock.get()))
		{
			int progress = state.getValue(HATCH_PROGRESS);
			if (this.canGrowProgressAtPosition(world, pos, state))
			{
				BlockPos abovePos = pos.above();
				if (progress < 15) // not hatchable yet
				{
					world.setBlockAndUpdate(pos, state.setValue(HATCH_PROGRESS, progress+1));
				}
				else if (!world.isDay()
					&& world.getBlockState(abovePos).getCollisionShape(world, abovePos).isEmpty()
					&& this.areEnoughPlayersNearToHatch(world, pos, state))
				{
					this.hatch(world, pos, state, random);
				}
			}
		}
	}
	
	protected void hatch(ServerLevel world, BlockPos pos, BlockState state, RandomSource random)
	{
		boolean persistant = state.getValue(PERSISTENT);
		world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());


       FroglinEntity froglin = Froglins.INSTANCE.froglinEntityType.get().create(world);
       froglin.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360F, 0.0F);
       if (persistant)
       {
    	   froglin.setPersistenceRequired();
       }
       world.addFreshEntity(froglin);
	}

	public boolean canGrowProgressAtPosition(LevelAccessor world, BlockPos pos, BlockState state)
	{
		return world.getFluidState(pos).getType() == Fluids.WATER
			&& this.canSurvive(state, world, pos);
	}
	
	public boolean areEnoughPlayersNearToHatch(EntityGetter world, BlockPos pos, BlockState state)
	{
		return state.getValue(PERSISTENT)
			// the boolean arg at the end of getClosestPlayer *ignores* creative players if true
			|| world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), MobCategory.MONSTER.getNoDespawnDistance(), false) != null;
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
	public boolean isValidBonemealTarget(BlockGetter world, BlockPos pos, BlockState state, boolean isClient)
	{
		return world instanceof Level && this.canDefinitelyUseBonemeal((Level)world, pos, state);
	}

	// called immediately after canGrow, only on servers
	// if this returns true, grow will be called
	@Override
	public boolean isBonemealSuccess(Level world, RandomSource rand, BlockPos pos, BlockState state)
	{
		return this.canDefinitelyUseBonemeal(world, pos, state);
	}
	
	protected boolean canDefinitelyUseBonemeal(Level world, BlockPos pos, BlockState state)
	{
		// make sure this block is correct
		if (state.is(Froglins.INSTANCE.froglinEggBlock.get()))
		{
			BlockPos abovePos = pos.above();
			return this.canGrowProgressAtPosition(world, pos, state)
				&& world.getBlockState(abovePos).getCollisionShape(world, abovePos).isEmpty();
		}
		else
		{
			return false;
		}
	}

	// called when canUseBonemeal returns true on server
	@Override
	public void performBonemeal(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state)
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
			world.setBlockAndUpdate(pos, state.setValue(HATCH_PROGRESS, progress + progressIncrease));
		}
		else
		{
			this.hatch(world, pos, state, rand);
		}
		
	}
	
	

}
