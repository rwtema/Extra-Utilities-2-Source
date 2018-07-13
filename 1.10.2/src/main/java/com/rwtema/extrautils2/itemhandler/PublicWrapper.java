package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public abstract class PublicWrapper implements IItemHandlerModifiableCompat {
	public final IItemHandlerModifiable handler;

	public PublicWrapper(IItemHandlerModifiable handler) {
		this.handler = handler;
	}

	@Override
	public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
		handler.setStackInSlot(slot, stack);
	}

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
		return handler.insertItem(slot, stack, simulate);
	}

	@ItemStackNonNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return handler.extractItem(slot, amount, simulate);
	}

	public static class Extract extends PublicWrapper {

		public Extract(IItemHandlerModifiable handler) {
			super(handler);
		}

		@ItemStackNonNull
		@Override
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			return stack;
		}
	}

	public static class Insert extends PublicWrapper {

		public Insert(IItemHandlerModifiable handler) {
			super(handler);
		}

		@ItemStackNonNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return StackHelper.empty();
		}
	}

}
