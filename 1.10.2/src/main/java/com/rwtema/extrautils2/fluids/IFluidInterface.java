package com.rwtema.extrautils2.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public interface IFluidInterface {
	int fill(FluidStack resource, boolean doFill);

	FluidStack drain(FluidStack resource, boolean doDrain);

	FluidStack drain(int maxDrain, boolean doDrain);

	FluidTankInfo[] getTankInfo();


}
