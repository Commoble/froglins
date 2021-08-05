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
		this.setFlags(EnumSet.of(Goal.Flag.JUMP));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state
	 * necessary for execution in this method as well.
	 */
	@Override
	public boolean canUse()
	{
		LivingEntity target = this.froglin.getTarget();
		return target != null
			&& this.froglin.isInWater();
	}

	@Override
	public void tick()
	{
		LivingEntity target = this.froglin.getTarget();
		if (target != null && (!target.isInWater() || this.froglin.getY() < target.getY()) && this.froglin.getRandom().nextFloat() < 0.8F)
		{
			this.froglin.getJumpControl().jump();
		}

	}
}
