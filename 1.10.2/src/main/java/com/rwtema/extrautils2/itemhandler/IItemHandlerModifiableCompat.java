package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public interface IItemHandlerModifiableCompat extends IItemHandlerModifiable, IItemHandlerCompat {
	@Override
	void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack);
}
