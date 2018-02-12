package com.rwtema.extrautils2.compatibility;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

public class ShapelessOreCompat extends ShapelessOreRecipe {

	public ShapelessOreCompat(ItemStack result, Object... recipe) {
		super(result, recipe);
	}


	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		return getRemainingItemsBase(inv);
	}

	@Nonnull
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv){
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}
}
