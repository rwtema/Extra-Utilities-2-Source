package com.rwtema.extrautils2.compatibility;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class ShapedOreCompat extends ShapedOreRecipe {

	public ShapedOreCompat(@Nonnull ItemStack result, Object... recipe) {
		super(result, recipe);
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
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv){
		return ForgeHooks.defaultRecipeGetRemainingItems(inv).toArray(new ItemStack[inv.getSizeInventory()]);
	}
}
