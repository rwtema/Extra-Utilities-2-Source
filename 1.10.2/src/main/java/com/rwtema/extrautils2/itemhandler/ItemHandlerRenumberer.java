package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Iterator;

public abstract class ItemHandlerRenumberer implements IItemHandlerCompat {
	final IItemHandler base;

	protected ItemHandlerRenumberer(IItemHandler base) {
		this.base = base;
	}

	public abstract int getRenumberedSlots(int slot, int num_slots);
	public int getRenumberedSlot(int slot){
		return getRenumberedSlots(slot, getSlots());
	}

	@Override
	public int getSlots() {
		return base.getSlots();
	}

	@ItemStackNonNull
	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		return base.getStackInSlot(getRenumberedSlot(slot));
	}

	@ItemStackNonNull
	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		return base.insertItem(getRenumberedSlot(slot), stack, simulate);
	}

	@ItemStackNonNull
	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return base.extractItem(getRenumberedSlot(slot), amount, simulate);
	}


}
