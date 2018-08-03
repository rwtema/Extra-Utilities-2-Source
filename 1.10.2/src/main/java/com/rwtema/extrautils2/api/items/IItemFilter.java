package com.rwtema.extrautils2.api.items;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IItemFilter {
	boolean isItemFilter(@Nonnull ItemStack filterStack);

	boolean matches(@Nonnull ItemStack filterStack, ItemStack target);
}
