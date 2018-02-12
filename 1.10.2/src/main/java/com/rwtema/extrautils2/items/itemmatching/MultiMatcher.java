package com.rwtema.extrautils2.items.itemmatching;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

public class MultiMatcher implements IMatcher {
	Collection<? extends Predicate<ItemStack>> matchers;

	public MultiMatcher(Collection<? extends Predicate<ItemStack>> matchers) {
		this.matchers = matchers;
	}

	@Override
	public boolean matchesItemStack(@Nullable ItemStack t) {
		for (Predicate<ItemStack> matcher : matchers) {
			if (matcher.test(t))
				return true;
		}
		return false;
	}
}
