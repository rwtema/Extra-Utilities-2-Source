package com.rwtema.extrautils2.compatibility;

import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;

public class DataWatcherItemStack {

	public static ItemStack getStack(EntityDataManager dataManager, Wrapper key) {
		return dataManager.get(key.wrapper);
	}

	public static void setStack(EntityDataManager dataManager, ItemStack stack, Wrapper key) {
		dataManager.set(key.wrapper, stack);
	}

	public static void register(EntityDataManager dataManager, Wrapper key) {
		dataManager.register(key.wrapper, ItemStack.EMPTY);
	}

	public static class Wrapper {
		public final DataParameter<ItemStack> wrapper;

		public Wrapper(DataParameter<ItemStack> wrapper) {
			this.wrapper = wrapper;
		}
	}
}
