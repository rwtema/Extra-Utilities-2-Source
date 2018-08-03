package com.rwtema.extrautils2.api.fluids;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public interface IFluidFilter {
	boolean isFluidFilter(@Nonnull ItemStack filterStack);

	boolean matches(@Nonnull ItemStack filterStack, FluidStack target);
}
