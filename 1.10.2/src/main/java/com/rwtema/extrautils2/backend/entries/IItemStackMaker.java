package com.rwtema.extrautils2.backend.entries;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.Supplier;

public interface IItemStackMaker extends Supplier<ItemStack> {
	ItemStack newStack();

	@Override
	default ItemStack get() {
		return newStack();
	}

	default ItemStack newStack(int amount) {
		ItemStack stack = newStack();
		return ItemHandlerHelper.copyStackWithSize(stack, amount);
	}
}
