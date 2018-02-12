package com.rwtema.extrautils2.items.itemmatching;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface IMatcher extends Predicate<ItemStack> {
	boolean matchesItemStack(@Nullable ItemStack t);

	@Override
	default boolean test(ItemStack stack) {
		return matchesItemStack(stack);
	}
}
