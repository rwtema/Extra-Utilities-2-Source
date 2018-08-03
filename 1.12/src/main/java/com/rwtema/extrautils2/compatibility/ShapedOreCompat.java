package com.rwtema.extrautils2.compatibility;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class ShapedOreCompat extends ShapedOreRecipe implements RecipeCompat {

	public ShapedOreCompat(ResourceLocation location, @Nonnull ItemStack result, Object... recipe) {
		super(location, result, recipe);
		setRegistryName(location);
	}

	@Override
	public final NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		ItemStack[] remainingItemsBase = getRemainingItemsBase(inv);
		NonNullList<ItemStack> stacks = NonNullList.withSize(remainingItemsBase.length, ItemStack.EMPTY);
		for (int i = 0; i < remainingItemsBase.length; i++) {
			stacks.set(i, remainingItemsBase[i]);
		}
		return stacks;
	}

	@Nonnull
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		return ForgeHooks.defaultRecipeGetRemainingItems(inv).toArray(new ItemStack[inv.getSizeInventory()]);
	}

	public void setInput(Object[] input) {
		throw new IllegalStateException();
	}
}
