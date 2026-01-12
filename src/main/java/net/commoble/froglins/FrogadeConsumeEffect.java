package net.commoble.froglins;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

public enum FrogadeConsumeEffect implements ConsumeEffect
{
	INSTANCE;
	
	public static final MapCodec<FrogadeConsumeEffect> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, FrogadeConsumeEffect> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	
	@Override
	public Type<? extends ConsumeEffect> getType()
	{
		return null;
	}
	
	@Override
	public boolean apply(Level level, ItemStack stack, LivingEntity entity)
	{
		if (!entity.isInvertedHealAndHarm())
		{
			float missingHealth = entity.getMaxHealth() - entity.getHealth(); 
			entity.heal((float)Math.floor(Math.sqrt(missingHealth)));
			
			if (!entity.level().isClientSide())
			{
				BuiltInRegistries.MOB_EFFECT.getTagOrEmpty(Froglins.CURABLE_AILMENTS_TAG)
					.forEach(effectHolder -> entity.removeEffect(effectHolder));
				
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
		
		return true;
	}
}
