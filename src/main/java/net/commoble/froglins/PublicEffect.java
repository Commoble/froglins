package net.commoble.froglins;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class PublicEffect extends MobEffect
{
	// Effect constructor is protected
	public PublicEffect(MobEffectCategory typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

}
