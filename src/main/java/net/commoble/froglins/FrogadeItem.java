package net.commoble.froglins;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class FrogadeItem extends Item
{
	public static final Component TOOLTIP_0 = Component.translatable("tooltip.froglins.frogade_0").withStyle(s -> s.applyFormat(ChatFormatting.GRAY));
	public static final Component TOOLTIP_1 = Component.translatable("tooltip.froglins.frogade_1").withStyle(s -> s.applyFormat(ChatFormatting.GRAY));
	public static final Component TOOLTIP_2 = Component.translatable("tooltip.froglins.frogade_2").withStyle(s -> s.applyFormat(ChatFormatting.GRAY));
	
	public FrogadeItem(Properties properties)
	{
		super(properties);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag flag)
	{
		super.appendHoverText(stack, context, display, components, flag);
		components.accept(TOOLTIP_0);
		components.accept(TOOLTIP_1);
		components.accept(TOOLTIP_2);
	}
	
	

}
