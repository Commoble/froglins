package commoble.froglins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.potion.InstantEffect;

public class HealthinessEffect extends InstantEffect
{

	public HealthinessEffect(EffectType type, int liquidColor)
	{
		super(type, liquidColor);
	}

	@Override
	public void performEffect(LivingEntity entity, int amplifier)
	{
		if (!entity.isEntityUndead())
		{
			float missingHealth = entity.getMaxHealth() - entity.getHealth(); 
			entity.heal((float)Math.floor(Math.sqrt(missingHealth)));
			
			if (!entity.world.isRemote())
			{
				entity.removePotionEffect(Effects.BLINDNESS);
				entity.removePotionEffect(Effects.NAUSEA);
				entity.removePotionEffect(Effects.POISON);
			}
		}
	}
	
	

}
