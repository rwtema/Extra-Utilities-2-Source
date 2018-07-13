package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.RecipeCompat;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class RecipeBase implements RecipeCompat {
	@Nonnull
	@Override
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		return RecipeCompat.defaultRecipeGetRemainingItems(inv);
	}
}
