package commoble.froglins.ai;

import java.util.EnumSet;

import commoble.froglins.FroglinEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SwimGoal;

public class SwimToTargetGoal extends SwimGoal
{
	private final FroglinEntity froglin; 

	public SwimToTargetGoal(FroglinEntity froglin)
	{
		super(froglin);
		this.froglin = froglin;
		this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state
	 * necessary for execution in this method as well.
	 */
	@Override
	public boolean shouldExecute()
	{
		LivingEntity target = this.froglin.getAttackTarget();
		return target != null
			&& this.froglin.isInWater()
			&& this.froglin.getPosY() < target.getPosY();
	}

	@Override
	public void tick()
	{
		if (this.froglin.getRNG().nextFloat() < 0.8F)
		{
			this.froglin.getJumpController().setJumping();
		}

	}
}
