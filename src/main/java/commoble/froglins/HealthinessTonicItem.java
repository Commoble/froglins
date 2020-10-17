package commoble.froglins;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HealthinessTonicItem extends Item
{
	public static final ITextComponent TOOLTIP_0 = new TranslationTextComponent("tooltip.froglins.tonic_of_healthiness_0").modifyStyle(s -> s.applyFormatting(TextFormatting.GRAY));
	public static final ITextComponent TOOLTIP_1 = new TranslationTextComponent("tooltip.froglins.tonic_of_healthiness_1").modifyStyle(s -> s.applyFormatting(TextFormatting.GRAY));
	public static final ITextComponent TOOLTIP_2 = new TranslationTextComponent("tooltip.froglins.tonic_of_healthiness_2").modifyStyle(s -> s.applyFormatting(TextFormatting.GRAY));
	
	public HealthinessTonicItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.DRINK;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entity)
	{
		super.onItemUseFinish(stack, world, entity);
		PlayerEntity player = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
		
		if (player == null || !player.abilities.isCreativeMode)
		{
			if (stack.isEmpty())
			{
				return this.getContainerItem(stack);
			}

			if (player != null)
			{
				player.inventory.addItemStackToInventory(this.getContainerItem(stack));
			}
		}

		return stack;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TOOLTIP_0);
		tooltip.add(TOOLTIP_1);
		tooltip.add(TOOLTIP_2);
	}

}
