package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class ItemHandlerFilterInsertion<T extends IItemHandlerModifiableCompat> implements IItemHandlerModifiableCompat {
	public final T original;

	public ItemHandlerFilterInsertion(T original) {
		this.original = original;
	}

	public abstract boolean isValid(@Nonnull ItemStack stack);

	@Override
	public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
		original.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return original.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return original.getStackInSlot(slot);
	}

	@Override
	@ItemStackNonNull
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		if (StackHelper.isNull(stack) || !isValid(stack)) return stack;
		return original.insertItem(slot, stack, simulate);
	}

	@Override
	@ItemStackNonNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack extractItem = original.extractItem(slot, amount, simulate);
		if (StackHelper.isNull(extractItem) || isValid(extractItem)) return StackHelper.empty();
		return extractItem;
	}

	@Override
	public int getSlotLimit(int slot) {
		return original.getSlotLimit(slot);
	}

	public IItemHandlerModifiableCompat getGUIVariant() {
		return new ItemHandlerFilterInsertion<T>(original) {
			@Override
			public boolean isValid(@Nonnull ItemStack stack) {
				return ItemHandlerFilterInsertion.this.isValid(stack);
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return original.extractItem(slot, amount, simulate);
			}
		};
	}
}
