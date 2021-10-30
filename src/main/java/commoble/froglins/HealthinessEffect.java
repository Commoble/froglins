package commoble.froglins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class HealthinessEffect extends InstantenousMobEffect
{
	public static final ResourceLocation CURABLE_AILMENTS_ID = new ResourceLocation(Froglins.MODID, "healthiness_tonic_curable_effects");
	
	public HealthinessEffect(MobEffectCategory type, int liquidColor)
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
				Froglins.INSTANCE.mobEffectTags.getTag(CURABLE_AILMENTS_ID)
					.getValues()
					.forEach(entity::removeEffect);
				
				if (entity instanceof Player)
				{
					Player player = (Player)entity;
					FoodData foodStats = player.getFoodData();
					
					int currentFood = foodStats.getFoodLevel();
					int maxFood = 20;
					double missingFood = maxFood - currentFood;
					int foodRestored = Mth.floor(Math.sqrt(missingFood));
					foodStats.eat(foodRestored, 0.0F);
				}
			}
		}
	}
	
	

}
