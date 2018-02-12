package com.rwtema.extrautils2.power.energy;

import net.minecraftforge.energy.IEnergyStorage;

public abstract class PublicEnergyWrapper implements IEnergyStorage {
	final IEnergyStorage base;

	@Override
	public int getEnergyStored() {
		return base.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored() {
		return base.getMaxEnergyStored();
	}

	public PublicEnergyWrapper(IEnergyStorage base) {
		this.base = base;
	}

	public static class View extends PublicEnergyWrapper {

		public View(IEnergyStorage base) {
			super(base);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	}

	public static class Receive extends PublicEnergyWrapper {

		public Receive(IEnergyStorage base) {
			super(base);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return base.receiveEnergy(maxReceive, simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	}

	public static class Extract extends PublicEnergyWrapper {

		public Extract(IEnergyStorage base) {
			super(base);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return base.extractEnergy(maxExtract, simulate);
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	}
}
