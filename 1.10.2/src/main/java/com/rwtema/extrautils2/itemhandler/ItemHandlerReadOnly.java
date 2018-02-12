package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerReadOnly implements IItemHandlerCompat {
	IItemHandler handler;

	@Override
	public int getSlots() {
		return handler.getSlots();
	}

	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return handler.getStackInSlot(slot);
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		return stack;
	}

	@ItemStackNonNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return StackHelper.empty();
	}
}
