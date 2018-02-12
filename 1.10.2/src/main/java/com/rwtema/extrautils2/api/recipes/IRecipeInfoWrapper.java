package com.rwtema.extrautils2.api.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nullable;
import java.util.List;

public interface IRecipeInfoWrapper<T extends IRecipe> extends IRecipe {
	T getOriginalRecipe();

	String info();

	List<List<ItemStack>> getInputList();

	@Nullable
	 int[] getDimensions();
}