package com.rwtema.extrautils2.api.resonator;

import net.minecraft.item.ItemStack;

import java.util.function.Function;

public class ResonatorRecipes {
	public static Function<IResonatorRecipe, Boolean> registerRecipe = s -> false;

	public static Function<ItemStack, IResonatorRecipe> removeRecipe = s -> null;
}
