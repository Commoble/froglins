package net.commoble.froglins.ai;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Wrapper class around another Goal that allows the delegated goal to start or continue executing
 * when its given condition is true for its owner entity.
 * 
 * Note that the given condition will be evaluated before the delegate.
 * If the condition fails in shouldContinueExecuting, then resetTask() will be called on the delegate.
 */
public class PredicatedGoal<E extends LivingEntity> extends Goal
{
	private final Goal delegate;
	private final E entity;
	private final Predicate<E> predicate;
	
	public PredicatedGoal(E entity, Goal delegate, Predicate<E> predicate)
	{
		this.entity = entity;
		this.delegate = delegate;
		this.predicate = predicate;
	}

	@Override
	public boolean canUse()
	{
		return this.predicate.test(this.entity) && this.delegate.canUse();
	}

	@Override
	public boolean canContinueToUse()
	{
		if (!this.predicate.test(this.entity))
		{
			this.delegate.stop();
			return false;
		}
		return this.delegate.canContinueToUse();
	}
	
	
	
	@Override
	public boolean isInterruptable()
	{
		return this.delegate.isInterruptable();
	}

	@Override
	public void start()
	{
		this.delegate.start();
	}

	@Override
	public void stop()
	{
		this.delegate.stop();
	}

	@Override
	public void tick()
	{
		this.delegate.tick();
	}

	@Override
	public void setFlags(EnumSet<Flag> flagSet)
	{
		this.delegate.setFlags(flagSet);
	}

	@Override
	public EnumSet<Flag> getFlags()
	{
		return this.delegate.getFlags();
	}

	@Override
	public String toString()
	{
		return String.format("%s(%s)", super.toString(), this.delegate.toString());
	}
}
