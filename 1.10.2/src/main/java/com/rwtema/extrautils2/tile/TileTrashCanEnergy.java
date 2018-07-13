package com.rwtema.extrautils2.tile;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class TileTrashCanEnergy extends XUTile {

	IEnergyStorage ABSORB_HANDLER = new IEnergyStorage() {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return maxReceive;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 1000000;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	};

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return ABSORB_HANDLER;
	}
}
