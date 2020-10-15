package commoble.froglins;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.IGrowable;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

public class FroglinEggBlock extends Block implements IBucketPickupHandler, ILiquidContainer, IGrowable
{
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty PERSISTANT = BooleanProperty.create("persistant");
	public static final IntegerProperty HATCH_PROGRESS = BlockStateProperties.AGE_0_15;
	
	public static final VoxelShape SHAPE = Block.makeCuboidShape(2D, 0D, 2D, 14D, 6D, 14D);

	public FroglinEggBlock(Properties properties)
	{
		super(properties);
		BlockState defaultState = this.stateContainer.getBaseState()
			.with(WATERLOGGED, false)
			.with(PERSISTANT, false)
			.with(HATCH_PROGRESS, 0);
		this.setDefaultState(defaultState);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(WATERLOGGED, PERSISTANT, HATCH_PROGRESS);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		BlockPos placePos = context.getPos();
		FluidState fluidState = context.getWorld().getFluidState(placePos);
		BlockState stateToPlace = super.getStateForPlacement(context).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		if (this.isValidPosition(stateToPlace, context.getWorld(), placePos))
		{
			return stateToPlace;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		BlockPos belowPos = pos.down();
		return this.isValidGround(worldIn.getBlockState(belowPos), worldIn, belowPos);
	}

	protected boolean isValidGround(BlockState belowState, IBlockReader worldIn, BlockPos belowPos)
	{
		return belowState.isOpaqueCube(worldIn, belowPos);
	}
	
	public boolean isPositionValidAndInWater(IWorldReader world, BlockPos pos)
	{
		return world.hasWater(pos)
			&& world.getBlockState(pos).getMaterial().isReplaceable()
			&& this.isValidPosition(this.getDefaultState(), world, pos);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if (!stateIn.isValidPosition(worldIn, currentPos))
		{
			return Blocks.AIR.getDefaultState();
		}
		else
		{
			if (stateIn.get(WATERLOGGED))
			{
				worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
			}

			@SuppressWarnings("deprecation")
			BlockState result = super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
			return result;
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public OffsetType getOffsetType()
	{
		return OffsetType.XYZ;
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		// make sure this block is correct
		if (state.isIn(Froglins.INSTANCE.froglinEggBlock.get()))
		{
			int progress = state.get(HATCH_PROGRESS);
			if (this.canGrowProgressAtPosition(world, pos, state))
			{
				BlockPos abovePos = pos.up();
				if (progress < 15) // not hatchable yet
				{
					world.setBlockState(pos, state.with(HATCH_PROGRESS, progress+1));
				}
				else if (!world.isDaytime()
					&& world.getBlockState(abovePos).getCollisionShape(world, abovePos).isEmpty()
					&& this.areEnoughPlayersNearToHatch(world, pos, state))
				{
					this.hatch(world, pos, state, random);
				}
			}
		}
	}
	
	protected void hatch(ServerWorld world, BlockPos pos, BlockState state, Random random)
	{
		boolean persistant = state.get(PERSISTANT);
		world.setBlockState(pos, Blocks.WATER.getDefaultState());


       FroglinEntity froglin = Froglins.INSTANCE.froglin.create(world);
       froglin.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360F, 0.0F);
       if (persistant)
       {
    	   froglin.enablePersistence();
       }
       world.addEntity(froglin);
	}

	public boolean canGrowProgressAtPosition(IWorld world, BlockPos pos, BlockState state)
	{
		return world.getFluidState(pos).getFluid() == Fluids.WATER
			&& this.isValidPosition(state, world, pos);
	}
	
	public boolean areEnoughPlayersNearToHatch(IEntityReader world, BlockPos pos, BlockState state)
	{
		return state.get(PERSISTANT)
			// the boolean arg at the end of getClosestPlayer *ignores* creative players if true
			|| world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), EntityClassification.MONSTER.getRandomDespawnDistance(), false) != null;
	}

	// fluid crunk

	@Override
	public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
	{
		return !state.get(WATERLOGGED) && fluidIn == Fluids.WATER;
	}

	@Override
	public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn)
	{
		if (!state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER)
		{
			if (!worldIn.isRemote())
			{
				worldIn.setBlockState(pos, state.with(WATERLOGGED, true), Constants.BlockFlags.DEFAULT);
				worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state)
	{
		if (state.get(WATERLOGGED))
		{
			worldIn.setBlockState(pos, state.with(WATERLOGGED, false), Constants.BlockFlags.DEFAULT);
			return Fluids.WATER;
		}
		else
		{
			return Fluids.EMPTY;
		}
	}

	@Override
	@Deprecated
	public FluidState getFluidState(BlockState state)
	{
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}
	
	// bonemeal crunk

	// called first, on both server/client
	// if this returns true, bonemeal stack will shrink and animation will play
	@Override
	public boolean canGrow(IBlockReader world, BlockPos pos, BlockState state, boolean isClient)
	{
		return world instanceof World && this.canDefinitelyUseBonemeal((World)world, pos, state);
	}

	// called immediately after canGrow, only on servers
	// if this returns true, grow will be called
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state)
	{
		return this.canDefinitelyUseBonemeal(world, pos, state);
	}
	
	protected boolean canDefinitelyUseBonemeal(World world, BlockPos pos, BlockState state)
	{
		// make sure this block is correct
		if (state.isIn(Froglins.INSTANCE.froglinEggBlock.get()))
		{
			BlockPos abovePos = pos.up();
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
	public void grow(ServerWorld world, Random rand, BlockPos pos, BlockState state)
	{
		int progress = state.get(HATCH_PROGRESS);
		int progressRemaining = 15 - progress;
		if (progressRemaining > 0) // not time to hatch yet
		{
			// let's say we are at stage 14
			// progressRemaining = 1
			// we can advance by [0 or 1] stages
			// so we need to call nextInt(2)
			// which is progressRemaining+1
			int progressIncrease = rand.nextInt(progressRemaining+1);
			world.setBlockState(pos, state.with(HATCH_PROGRESS, progress + progressIncrease));
		}
		else
		{
			this.hatch(world, pos, state, rand);
		}
		
	}
	
	

}
