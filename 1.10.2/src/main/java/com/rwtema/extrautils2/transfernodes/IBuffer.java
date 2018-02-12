package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import jline.internal.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IBuffer {
	@ItemStackNonNull
	ItemStack getItem();

	@Nullable
	FluidStack getFluid();

	Type getBufferType();

	enum Type {
		FLUID, ITEM
	}
}
