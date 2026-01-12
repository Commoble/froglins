package net.commoble.froglins.ai;

import org.jspecify.annotations.Nullable;

import net.commoble.froglins.FroglinEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

// from Drowned, helps prevent froglin pathfinding getting stuck
public class SwimToTargetGoal extends Goal {
    private final FroglinEntity froglin;
    private boolean stuck = false;

    public SwimToTargetGoal(FroglinEntity froglin) {
        this.froglin = froglin;
    }

    @Override
    public boolean canUse() {
    	return this.froglin.getTarget() != null && this.froglin.isInWater();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && !this.stuck;
    }

    @Override
    public void tick() {
    	@Nullable LivingEntity target = this.froglin.getTarget();
        if (target != null && (this.froglin.getNavigation().isDone() || this.froglin.closeToNextPos())) {
        	
            Vec3 vec3 = DefaultRandomPos.getPosTowards(
                this.froglin, 4, 8, this.froglin.getTarget().position(), (float) (Math.PI / 2)
            );
            if (vec3 == null) {
                this.stuck = true;
                return;
            }

            this.froglin.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1F);
        }
    }

    @Override
    public void start() {
        this.stuck = false;
    }
}
