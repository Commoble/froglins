package commoble.froglins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class PotionUpgradeRecipe implements IBrewingRecipe
{
	private final ITag<Potion> inputPotionTag;
	private final ITag<Item> ingredientTag;
	private final Potion output;
	
	public PotionUpgradeRecipe(ITag<Potion> inputPotionTag, ITag<Item> ingredientTag, Potion output)
	{
		this.inputPotionTag = inputPotionTag;
		this.ingredientTag = ingredientTag;
		this.output = output;
	}
	
	@Override
	public boolean isInput(ItemStack input)
	{
		return this.inputPotionTag.contains(PotionUtils.getPotionFromItem(input));
	}

	@Override
	public boolean isIngredient(ItemStack ingredient)
	{
		return this.ingredientTag.contains(ingredient.getItem());
	}

	@Override
	public ItemStack getOutput(ItemStack input, ItemStack ingredient)
	{
		return this.isInput(input) && this.isIngredient(ingredient)
			? PotionUtils.addPotionToItemStack(input.copy(), this.output)
			: ItemStack.EMPTY;
	}

}
