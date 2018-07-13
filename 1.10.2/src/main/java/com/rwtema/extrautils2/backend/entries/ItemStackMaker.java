package com.rwtema.extrautils2.backend.entries;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemStackMaker {
	private final LoadingCache<ItemRef, IItemStackMaker> cache = CacheBuilder.newBuilder().build(new CacheLoader<ItemRef, IItemStackMaker>() {
		@Override
		public IItemStackMaker load(@Nonnull final ItemRef key) throws Exception {
			return new IItemStackMaker() {
				@Override
				public ItemStack newStack() {
					return key.createItemStack(1);
				}
			};
		}
	});

	public IItemStackMaker of(ItemStack stack) {
		return cache.getUnchecked(ItemRef.wrap(stack));
	}
}
