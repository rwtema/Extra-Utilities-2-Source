package com.rwtema.extrautils2.api.machine;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class MachineSlotFluid extends MachineSlot<FluidStack> {

	@Nullable
	public final FluidStack filterStack;

	public MachineSlotFluid(String name) {
		this(name, 1000, null);
	}

	public MachineSlotFluid(String name, int stackCapacity, @Nullable FluidStack filterStack) {
		this(name, stackCapacity, false, filterStack);
	}

	public MachineSlotFluid(String name, int stackCapacity, boolean optional, @Nullable FluidStack filterStack) {
		this(name, stackCapacity, -1, optional, filterStack);
	}

	public MachineSlotFluid(String name, int stackCapacity, int color, boolean optional, @Nullable FluidStack filterStack) {
		super(name, color, optional, stackCapacity);
		this.filterStack = filterStack;
	}

	public MachineSlotFluid(String name, int stackCapacity) {
		this(name, stackCapacity, null);
	}

	@Override
	public String toString() {
		return "SlotFluid{" + name + "}";
	}

	public boolean matchesFluidInput(FluidStack stack) {
		return filterStack == null || filterStack.isFluidEqual(stack);
	}
}
