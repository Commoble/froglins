package commoble.froglins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.potion.InstantEffect;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.MathHelper;

public class HealthinessEffect extends InstantEffect
{

	public HealthinessEffect(EffectType type, int liquidColor)
	{
		super(type, liquidColor);
	}

	// this is called every tick
	// for instant effects from food items,
	// the effect instance must be added to the food item with a duration of exactly 1 tick
	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier)
	{
		if (!entity.isInvertedHealAndHarm())
		{
			float missingHealth = entity.getMaxHealth() - entity.getHealth(); 
			entity.heal((float)Math.floor(Math.sqrt(missingHealth)));
			
			if (!entity.level.isClientSide())
			{
				entity.removeEffect(Effects.BLINDNESS);
				entity.removeEffect(Effects.CONFUSION);
				entity.removeEffect(Effects.POISON);
				entity.removeEffect(Effects.HUNGER);
				
				if (entity instanceof PlayerEntity)
				{
					PlayerEntity player = (PlayerEntity)entity;
					FoodStats foodStats = player.getFoodData();
					
					int currentFood = foodStats.getFoodLevel();
					int maxFood = 20;
					double missingFood = maxFood - currentFood;
					int foodRestored = MathHelper.floor(Math.sqrt(missingFood));
					foodStats.eat(foodRestored, 0.0F);
				}
			}
		}
	}
	
	

}
