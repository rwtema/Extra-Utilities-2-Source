package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class EmptyHandlerModifiable extends EmptyHandler implements IItemHandlerModifiableCompat {
	public final static EmptyHandlerModifiable INSTANCE = new EmptyHandlerModifiable();
	@Override
	public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {

	}
}
