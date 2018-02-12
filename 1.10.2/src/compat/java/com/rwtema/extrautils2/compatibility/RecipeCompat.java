package com.rwtema.extrautils2.compatibility;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;

public interface RecipeCompat extends IRecipe {

	static ItemStack[] defaultRecipeGetRemainingItems(InventoryCrafting inv) {
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	static int getRecipeSize(IRecipe recipe) {
		return recipe.func_77570_a();
	}

	@Nonnull
	ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv);

	@Nonnull
	@Override
	default ItemStack[] getRemainingItems(@Nonnull InventoryCrafting inv) {
		return getRemainingItemsBase(inv);
	}

	@Override
	default int func_77570_a() {
		return getRecipeSize();
	}

	int getRecipeSize();

	boolean canFit(int width, int height);
}
