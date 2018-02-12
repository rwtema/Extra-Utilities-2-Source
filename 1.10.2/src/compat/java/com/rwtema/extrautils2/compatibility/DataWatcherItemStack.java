package com.rwtema.extrautils2.compatibility;

import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;

public class DataWatcherItemStack {

	public static ItemStack getStack(EntityDataManager dataManager, Wrapper key) {
		return dataManager.get(key.wrapper).orNull();
	}

	public static void setStack(EntityDataManager dataManager, ItemStack stack, Wrapper key) {
		dataManager.set(key.wrapper, Optional.fromNullable(stack));
	}

	public static void register(EntityDataManager dataManager, Wrapper key) {
		dataManager.register(key.wrapper, Optional.absent());
	}

	public static class Wrapper {
		public DataParameter<Optional<ItemStack>> wrapper;

		public Wrapper(DataParameter<Optional<ItemStack>> wrapper) {
			this.wrapper = wrapper;
		}
	}
}
