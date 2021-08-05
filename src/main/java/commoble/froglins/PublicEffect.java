package commoble.froglins;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class PublicEffect extends MobEffect
{
	// Effect constructor is private
	public PublicEffect(MobEffectCategory typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

}
