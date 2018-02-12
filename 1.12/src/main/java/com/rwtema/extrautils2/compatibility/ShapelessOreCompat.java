package com.rwtema.extrautils2.compatibility;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

public class ShapelessOreCompat extends ShapelessOreRecipe implements RecipeCompat {

	public ShapelessOreCompat(ResourceLocation location, ItemStack result, Object... recipe) {
		super(location, result, recipe);
		setRegistryName(location);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		ItemStack[] remainingItemsBase = getRemainingItemsBase(inv);
		NonNullList<ItemStack> stacks = NonNullList.withSize(remainingItemsBase.length, ItemStack.EMPTY);
		for (int i = 0; i < remainingItemsBase.length; i++) {
			stacks.set(i, remainingItemsBase[i].copy());
		}
		return stacks;
	}

	@Nonnull
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		return ForgeHooks.defaultRecipeGetRemainingItems(inv).toArray(new ItemStack[inv.getSizeInventory()]);
	}
}
