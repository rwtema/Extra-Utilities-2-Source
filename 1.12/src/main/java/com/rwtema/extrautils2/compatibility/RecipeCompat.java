package com.rwtema.extrautils2.compatibility;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;

public interface RecipeCompat extends IRecipe {

	static ItemStack[] defaultRecipeGetRemainingItems(InventoryCrafting inv) {
		return ForgeHooks.defaultRecipeGetRemainingItems(inv).toArray(new ItemStack[inv.getSizeInventory()]);
	}

	static int getRecipeSize(IRecipe compat) {
		return 9;
	}

	@Nonnull
	ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv);

	@Nonnull
	@Override
	default NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
		ItemStack[] remainingItemsBase = getRemainingItemsBase(inv);
		NonNullList<ItemStack> stacks = NonNullList.withSize(remainingItemsBase.length, ItemStack.EMPTY);
		for (int i = 0; i < remainingItemsBase.length; i++) {
			stacks.set(i, remainingItemsBase[i]);
		}
		return stacks;
	}

	default int func_77570_a() {
		return getRecipeSize();
	}

	default int getRecipeSize() {
		return 9;
	}

}
