package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public interface IItemHandlerCompat extends IItemHandler {
	@Override
	int getSlots();

	@ItemStackNonNull
	@Override
	ItemStack getStackInSlot(int slot);

	@ItemStackNonNull
	@Override
	ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate);

	@ItemStackNonNull
	@Override
	ItemStack extractItem(int slot, int amount, boolean simulate);

	default int getSlotLimit(int slot) {
		return 64;
	}
}
