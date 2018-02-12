package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ConcatFixedLengthSingleSlot implements IItemHandlerCompat {
	IItemHandler[] slotHandlers;

	public ConcatFixedLengthSingleSlot(IItemHandler... handlers) {
		slotHandlers = handlers;
	}

	@Override
	public int getSlots() {
		return slotHandlers.length;
	}

	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return slotHandlers[slot].getStackInSlot(0);
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		return slotHandlers[slot].insertItem(0, stack, simulate);
	}

	@ItemStackNonNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return slotHandlers[slot].extractItem(0, amount, simulate);
	}
}

