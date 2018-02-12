package com.rwtema.extrautils2.itemhandler;

import java.util.Arrays;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ConcatFixedLength implements IItemHandlerModifiableCompat {
	final IItemHandler[] handlers;
	final int[] firstSlot;
	final int totalSlots;

	public ConcatFixedLength(IItemHandler... handlers) {
		this.handlers = handlers;
		firstSlot = new int[handlers.length];
		int v = 0;
		for (int i = 0; i < handlers.length; i++) {
			firstSlot[i] = v;
			v += handlers[i].getSlots();
		}
		totalSlots = v;
	}

	public static IItemHandler create(IItemHandler... handlers) {
		for (IItemHandler handler : handlers) {
			if (handler.getSlots() != 1) {
				return new ConcatFixedLength(handlers);
			}
		}
		return new ConcatFixedLengthSingleSlot(handlers);
	}

	@Override
	public int getSlots() {
		return totalSlots;
	}

	public int getHandlerIndex(int slot) {
		int i = Arrays.binarySearch(firstSlot, slot);
		return i >= 0 ? i : (-i - 2);
	}

	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		int i = getHandlerIndex(slot);
		return handlers[i].getStackInSlot(slot - firstSlot[i]);
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		int i = getHandlerIndex(slot);
		return handlers[i].insertItem(slot - firstSlot[i], stack, simulate);
	}

	@ItemStackNonNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		int i = getHandlerIndex(slot);
		return handlers[i].extractItem(slot - firstSlot[i], amount, simulate);
	}

	@Override
	public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
		int i = getHandlerIndex(slot);
		if (handlers[i] instanceof IItemHandlerModifiable) {
			((IItemHandlerModifiable) handlers[i]).setStackInSlot(slot - firstSlot[i], stack);
		} else
			throw new UnsupportedOperationException();
	}
}
