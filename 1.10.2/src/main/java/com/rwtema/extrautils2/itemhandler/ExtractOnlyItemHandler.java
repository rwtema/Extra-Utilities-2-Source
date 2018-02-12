package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ExtractOnlyItemHandler implements IItemHandlerCompat {
	IItemHandler p;

	@Override
	public int getSlots() {
		return p.getSlots();
	}

	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return p.getStackInSlot(slot);
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		return stack;
	}

	@ItemStackNonNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return p.extractItem(slot, amount, simulate);
	}
}
