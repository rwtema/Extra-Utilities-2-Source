package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class ItemStackHandlerBase implements IItemHandlerCompat, IItemHandlerModifiableCompat {

	@ItemStackNonNull
	public abstract ItemStack getStack(int slot);
	public abstract void setStack(int slot, ItemStack stack);
	
	@Override
	public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
		if (ItemStack.areItemStacksEqual(getStack(slot), stack))
			return;
		setStack(slot,stack);
		onContentsChanged(slot);
	}
	
	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return getStack(slot);
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		if (StackHelper.isNull(stack) || StackHelper.isEmpty(stack))
			return StackHelper.empty();

		ItemStack existing = getStack(slot);

		int limit = getStackLimit(slot, stack);

		if (StackHelper.isNonNull(existing)) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
				return stack;

			limit -= StackHelper.getStacksize(existing);
		}

		if (limit <= 0)
			return stack;

		boolean reachedLimit = StackHelper.getStacksize(stack) > limit;

		if (!simulate) {
			if (StackHelper.isNull(existing)) {
				setStack(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
			} else {
				StackHelper.increase(existing, reachedLimit ? limit : StackHelper.getStacksize(stack));
			}
			onContentsChanged(slot);
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, StackHelper.getStacksize(stack) - limit) : StackHelper.empty();
	}

	@ItemStackNonNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return StackHelper.empty();

		ItemStack existing = getStack(slot);

		if (StackHelper.isNull(existing))
			return StackHelper.empty();

		int toExtract = Math.min(amount, existing.getMaxStackSize());

		if (StackHelper.getStacksize(existing) <= toExtract) {
			if (!simulate) {
				setStack(slot, null);
				onContentsChanged(slot);
			}
			return existing;
		} else {
			if (!simulate) {
				setStack(slot, ItemHandlerHelper.copyStackWithSize(existing, StackHelper.getStacksize(existing) - toExtract));
				onContentsChanged(slot);
			}

			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	protected int getStackLimit(int slot, ItemStack stack) {
		return stack.getMaxStackSize();
	}

	protected void onContentsChanged(int slot) {

	}
}
