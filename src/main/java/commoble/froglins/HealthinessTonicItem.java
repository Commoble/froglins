package commoble.froglins;

import java.util.List;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.world.item.Item.Properties;

public class HealthinessTonicItem extends Item
{
	public static final Component TOOLTIP_0 = new TranslatableComponent("tooltip.froglins.tonic_of_healthiness_0").withStyle(s -> s.applyFormat(ChatFormatting.GRAY));
	public static final Component TOOLTIP_1 = new TranslatableComponent("tooltip.froglins.tonic_of_healthiness_1").withStyle(s -> s.applyFormat(ChatFormatting.GRAY));
	public static final Component TOOLTIP_2 = new TranslatableComponent("tooltip.froglins.tonic_of_healthiness_2").withStyle(s -> s.applyFormat(ChatFormatting.GRAY));
	
	public HealthinessTonicItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.DRINK;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity)
	{
		super.finishUsingItem(stack, world, entity);
		Player player = entity instanceof Player ? (Player) entity : null;
		
		if (player == null || !player.getAbilities().instabuild)
		{
			if (stack.isEmpty())
			{
				return this.getContainerItem(stack);
			}

			if (player != null)
			{
				player.getInventory().add(this.getContainerItem(stack));
			}
		}

		return stack;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(TOOLTIP_0);
		tooltip.add(TOOLTIP_1);
		tooltip.add(TOOLTIP_2);
	}

}
