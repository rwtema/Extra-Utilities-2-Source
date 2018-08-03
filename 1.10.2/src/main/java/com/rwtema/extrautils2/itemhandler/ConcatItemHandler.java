package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;

public class ConcatItemHandler implements IItemHandlerCompat {
	final IItemHandler[] handlers;

	public ConcatItemHandler(IItemHandler... handlers) {
		this.handlers = handlers;
	}

	public ConcatItemHandler(Collection<IItemHandler> handlers) {
		this.handlers = handlers.toArray(new IItemHandler[0]);
	}

	public static IItemHandler concatNonNull(IItemHandler... values) {
		boolean fixedLength = true;
		ArrayList<IItemHandler> nonNullHandlers = new ArrayList<>(values.length);
		for (IItemHandler value : values) {
			if (value != null && value != EmptyHandler.INSTANCE && value != EmptyHandlerModifiable.INSTANCE) {
				nonNullHandlers.add(value);

				IItemHandler checkHandler = value;
				while (checkHandler instanceof PublicWrapper) {
					checkHandler = ((PublicWrapper) checkHandler).handler;
				}

				if (!(checkHandler instanceof SingleStackHandler || checkHandler instanceof ItemStackHandler)) {
					fixedLength = false;
				}
			}
		}

		if (nonNullHandlers.isEmpty())
			return EmptyHandler.INSTANCE;

		if (nonNullHandlers.size() == 1) return nonNullHandlers.get(0);

		if (fixedLength)
			return ConcatFixedLength.create(nonNullHandlers.toArray(new IItemHandler[0]));

		return new ConcatItemHandler(nonNullHandlers);
	}


	@Override
	public int getSlots() {
		int size = 0;
		for (IItemHandler handler : handlers) {
			size += handler.getSlots();
		}
		return size;
	}

	public Pair<IItemHandler, Integer> getDestination(int slot) {
		for (IItemHandler handler : handlers) {
			int numSlots = handler.getSlots();
			if (slot < numSlots) {
				return Pair.of(handler, slot);
			}

			slot -= numSlots;
		}
		return null;
	}

	@ItemStackNonNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		Pair<IItemHandler, Integer> pair = getDestination(slot);
		return pair != null ? pair.getLeft().getStackInSlot(pair.getRight()) : StackHelper.empty();
	}

	@ItemStackNonNull
	@Override
	public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
		Pair<IItemHandler, Integer> pair = getDestination(slot);
		return pair != null ? pair.getLeft().insertItem(pair.getRight(), stack, simulate) : stack;
	}

	@ItemStackNonNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		Pair<IItemHandler, Integer> pair = getDestination(slot);
		return pair != null ? pair.getLeft().extractItem(pair.getRight(), amount, simulate) : StackHelper.empty();
	}
}
