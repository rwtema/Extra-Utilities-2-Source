package com.rwtema.extrautils2.crafting;

import javax.annotation.Nonnull;

import com.rwtema.extrautils2.compatibility.RecipeCompat;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.ForgeHooks;

public abstract class RecipeBase implements RecipeCompat {
	@Nonnull
	@Override
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		return RecipeCompat.defaultRecipeGetRemainingItems(inv);
	}
}
