package com.rwtema.extrautils2.api.fluids;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidFilter {
	boolean isFluidFilter(@Nonnull ItemStack filterStack);

	boolean matches(@Nonnull ItemStack filterStack, FluidStack target);
}
