package commoble.froglins.ai;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

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
	public boolean shouldExecute()
	{
		return this.predicate.test(this.entity) && this.delegate.shouldExecute();
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		if (!this.predicate.test(this.entity))
		{
			this.delegate.resetTask();
			return false;
		}
		return this.delegate.shouldContinueExecuting();
	}
	
	
	
	@Override
	public boolean isPreemptible()
	{
		return this.delegate.isPreemptible();
	}

	@Override
	public void startExecuting()
	{
		this.delegate.startExecuting();
	}

	@Override
	public void resetTask()
	{
		this.delegate.resetTask();
	}

	@Override
	public void tick()
	{
		this.delegate.tick();
	}

	@Override
	public void setMutexFlags(EnumSet<Flag> flagSet)
	{
		this.delegate.setMutexFlags(flagSet);
	}

	@Override
	public EnumSet<Flag> getMutexFlags()
	{
		return this.delegate.getMutexFlags();
	}
}
