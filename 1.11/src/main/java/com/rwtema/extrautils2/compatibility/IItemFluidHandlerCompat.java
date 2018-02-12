package com.rwtema.extrautils2.compatibility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public interface IItemFluidHandlerCompat extends IFluidHandler {
	@Nullable
	public static IItemFluidHandlerCompat getFluidHandler(ItemStack stack) {
		final IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack);
		if (fluidHandler == null) return null;

		return new IItemFluidHandlerCompat() {
			@Override
			public ItemStack getModifiedStack() {
				return fluidHandler.getContainer();
			}

			public IFluidTankProperties[] getTankProperties() {
				return fluidHandler.getTankProperties();
			}

			public int fill(FluidStack resource, boolean doFill) {
				return fluidHandler.fill(resource, doFill);
			}

			@Nullable
			public FluidStack drain(FluidStack resource, boolean doDrain) {
				return fluidHandler.drain(resource, doDrain);
			}

			@Nullable
			public FluidStack drain(int maxDrain, boolean doDrain) {
				return fluidHandler.drain(maxDrain, doDrain);
			}
		};
	}

	ItemStack getModifiedStack();
}
