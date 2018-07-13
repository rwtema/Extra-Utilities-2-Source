package com.rwtema.extrautils2.compatibility;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class ShapedOreCompat extends ShapedOreRecipe {

	public ShapedOreCompat(ItemStack result, Object... recipe) {
		super(result, recipe);
	}


	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		return getRemainingItemsBase(inv);
	}

	@Nonnull
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	public void setInput(Object[] input) {
		this.input = input;
	}
}
