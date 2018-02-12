package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public abstract class SingleStackHandlerBase implements IItemHandlerModifiableCompat, IItemHandlerUpdate {
	@ItemStackNonNull
	public abstract ItemStack getStack();

	public abstract void setStack( @ItemStackNonNull ItemStack stack);

	@Override
	public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
		if (ItemStack.areItemStacksEqual(getStack(), stack))
			return;
		setStack(stack);
		onContentsChanged();
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return getStack();
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		if (StackHelper.isNull(stack) || StackHelper.isEmpty(stack))
			return StackHelper.empty();

		int limit = getStackLimit(stack);

		if (limit <= 0) return stack;

		ItemStack curStack = getStack();
		if (StackHelper.isNonNull(curStack)) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, curStack))
				return stack;

			limit -= StackHelper.getStacksize(curStack);

			if (limit <= 0) return stack;
		}

		boolean reachedLimit = StackHelper.getStacksize(stack) > limit;

		stack = StackHelper.safeCopy(stack);

		if (!simulate) {
			if (StackHelper.isNull(curStack)) {
				setStack(reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
			} else {
				StackHelper.increase(curStack, reachedLimit ? limit : StackHelper.getStacksize(stack));
				setStack(curStack);
			}
			onContentsChanged();
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, StackHelper.getStacksize(stack) - limit) : StackHelper.empty();
	}

	@Override
	public void onChange(int index) {
		onContentsChanged();
	}

	@ItemStackNonNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return StackHelper.empty();

		ItemStack existing = getStack();

		if (StackHelper.isNull(existing))
			return StackHelper.empty();

		int toExtract = amount == 1 ? 1 : Math.min(amount, existing.getMaxStackSize());

		if (StackHelper.getStacksize(existing) <= toExtract) {
			if (!simulate) {
				setStack(StackHelper.empty());
				onContentsChanged();
			}
			return existing;
		} else {
			if (!simulate) {
				StackHelper.decrease(existing, toExtract);
				setStack(existing);
				onContentsChanged();
			}

			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	protected int getStackLimit(@Nonnull ItemStack stack) {
		return stack.getMaxStackSize();
	}

	protected void onContentsChanged() {

	}

	public boolean canInsertAll(ItemStack stack) {
		if (StackHelper.isNull(stack)) return false;
		ItemStack item = insertItem(0, stack, true);
		return StackHelper.isNull(item);
	}

	public boolean canExtractAll(int amount) {
		ItemStack item = extractItem(0, amount, true);
		return StackHelper.isNonNull(item) && StackHelper.getStacksize(item) == amount;
	}

	public boolean isFull() {
		ItemStack curStack = getStack();
		return StackHelper.isNonNull(curStack) && StackHelper.getStacksize(curStack) >= curStack.getMaxStackSize();
	}

	public boolean isEmpty() {
		ItemStack curStack = getStack();
		return StackHelper.isNull(curStack) || StackHelper.isEmpty(curStack);
	}

	public int getStackLevel(){
		ItemStack curStack = getStack();
		return StackHelper.isNull(curStack) ? 0 : StackHelper.getStacksize(curStack);
	}
}
