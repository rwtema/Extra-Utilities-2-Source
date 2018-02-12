package com.rwtema.extrautils2.power.energy;


import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class XUEnergyStorage extends EnergyStorage implements INBTSerializable<NBTTagInt> {


	public XUEnergyStorage(int capacity) {
		super(capacity);
	}

	public XUEnergyStorage(int capacity, int maxTransfer) {
		super(capacity, maxTransfer);
	}

	public XUEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract);
	}

	@Override
	public NBTTagInt serializeNBT() {
		if (energy < 0) energy = 0;
		return new NBTTagInt(energy);
	}

	@Override
	public void deserializeNBT(NBTTagInt nbt) {
		energy = nbt.getInt();
		if (energy > capacity) energy = capacity;
	}

	public XUEnergyStorage setCapacity(int capacity) {

		this.capacity = capacity;

		if (energy > capacity) {
			energy = capacity;
		}
		return this;
	}

	public XUEnergyStorage setMaxTransfer(int maxTransfer) {

		setMaxReceive(maxTransfer);
		setMaxExtract(maxTransfer);
		return this;
	}

	public XUEnergyStorage setMaxReceive(int maxReceive) {

		this.maxReceive = maxReceive;
		return this;
	}

	public XUEnergyStorage setMaxExtract(int maxExtract) {

		this.maxExtract = maxExtract;
		return this;
	}

	public int getMaxReceive() {

		return maxReceive;
	}

	public int getMaxExtract() {

		return maxExtract;
	}

	public void setEnergyStored(int energy) {

		this.energy = energy;

		if (this.energy > capacity) {
			this.energy = capacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}

	public void modifyEnergyStored(int energy) {

		this.energy += energy;

		if (this.energy > capacity) {
			this.energy = capacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}
}
