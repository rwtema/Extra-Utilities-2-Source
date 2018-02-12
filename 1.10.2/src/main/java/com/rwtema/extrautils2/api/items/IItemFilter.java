package com.rwtema.extrautils2.api.items;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public interface IItemFilter {
	boolean isItemFilter(@Nonnull ItemStack filterStack);

	boolean matches(@Nonnull ItemStack filterStack, ItemStack target);
}
