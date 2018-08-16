package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.utils.ItemStackNonNull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

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
