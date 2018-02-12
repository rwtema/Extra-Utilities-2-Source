package com.rwtema.extrautils2.fluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidTankSerial extends FluidTank implements INBTSerializable<NBTTagCompound> {
	public FluidTankSerial(int capacity) {
		super(capacity);
	}

	public FluidTankSerial(FluidStack stack, int capacity) {
		super(stack, capacity);
	}

	public FluidTankSerial(Fluid fluid, int amount, int capacity) {
		super(fluid, amount, capacity);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}

	public boolean isEmpty() {
		return fluid == null || fluid.amount == 0;
	}

	protected void onChangeFill() {
		onChange();
	}

	protected void onChange() {

	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		int fill = super.fill(resource, doFill);
		if (doFill && fill != 0) {
			onChangeFill();
		}
		return fill;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		FluidStack drain = super.drain(maxDrain, doDrain);
		if(doDrain && drain != null){
			onChangeDrain();
		}
		return drain;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		FluidStack drain = super.drain(resource, doDrain);
		if(doDrain && drain != null){
			onChangeDrain();
		}
		return drain;
	}

	protected void onChangeDrain() {
		onChange();
	}

	public boolean isFull() {
		return fluid != null && fluid.amount >= capacity;
	}
}
