package com.rwtema.extrautils2.api.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public abstract class XURecipeCreator {
	public XURecipeCreator creator = null;

	public abstract IRecipe newXUShapelessRecipe(ItemStack result, Object... recipe);

	public abstract IRecipe newXUShapedRecipe(ItemStack result, Object... recipe);


	public abstract <T extends IRecipe> IRecipeInfoWrapper<T> addJEIInfo(T recipe, String info);

	public abstract <T extends IRecipe> IRecipeInfoWrapper<T> addEnchantmentCraftingRequirement(T recipe, int xpLevels);
}
