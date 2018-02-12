package com.rwtema.extrautils2.fluids;

import net.minecraftforge.fluids.FluidStack;

public interface IOptionalFluid {
	boolean isPresent();

	FluidStack createStack(int amount);

	boolean matches(FluidStack stack);
}
