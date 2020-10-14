package commoble.froglins.ai;

import java.util.EnumSet;

import commoble.froglins.FroglinEntity;
import net.minecraft.entity.ai.goal.Goal;

public class JumpSometimesGoal extends Goal
{
	private final FroglinEntity froglin;
	
	public JumpSometimesGoal(FroglinEntity froglin)
	{
		this.froglin = froglin;
		this.setMutexFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean shouldExecute()
	{
		return !this.froglin.isBeingRidden()
			&& this.froglin.getRNG().nextInt(10) == 0
			&& this.froglin.getMotion().lengthSquared() > 0.001F
			&& !this.froglin.isInWater()
			&& this.froglin.isOnGround(); 
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return !this.froglin.isOnGround();
	}

	@Override
	public void startExecuting()
	{
		this.froglin.jump();
	}

}
