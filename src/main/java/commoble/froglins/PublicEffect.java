package commoble.froglins;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class PublicEffect extends Effect
{
	// Effect constructor is private
	public PublicEffect(EffectType typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

}
