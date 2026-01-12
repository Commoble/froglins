package net.commoble.froglins.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;

public class LeapAtTargetGoalNoFriction extends LeapAtTargetGoal
{
	private final Mob mob;
	public LeapAtTargetGoalNoFriction(Mob mob, float jumpForce)
	{
		super(mob, jumpForce);
		this.mob = mob;
	}

	@Override
	public void start()
	{
		this.mob.setDiscardFriction(true);
		super.start();
		this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(2F, 1F, 2F));
	}	
}
