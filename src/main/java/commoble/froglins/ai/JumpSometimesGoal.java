package commoble.froglins.ai;

import java.util.EnumSet;

import commoble.froglins.FroglinEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class JumpSometimesGoal extends Goal
{
	private final FroglinEntity froglin;
	
	public JumpSometimesGoal(FroglinEntity froglin)
	{
		this.froglin = froglin;
		this.setFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean canUse()
	{
		return !this.froglin.isVehicle()
			&& this.froglin.getRandom().nextInt(10) == 0
			&& this.froglin.getDeltaMovement().lengthSqr() > 0.001F
			&& !this.froglin.isInWater()
			&& this.froglin.isOnGround(); 
	}

	@Override
	public boolean canContinueToUse()
	{
		return !this.froglin.isOnGround();
	}

	@Override
	public void start()
	{
		this.froglin.jumpFromGround();
	}

}
