package com.rwtema.extrautils2.gui.backend;

import net.minecraftforge.energy.IEnergyStorage;

public class WidgetEnergyStorage extends WidgetEnergy {
	final IEnergyStorage storage;

	public WidgetEnergyStorage(int x, int y, IEnergyStorage storage) {
		super(x, y);
		this.storage = storage;
	}

	@Override
	public int getCurrentEnergy() {
		return storage.getEnergyStored();
	}

	@Override
	public int getTotalEnergy() {
		return storage.getMaxEnergyStored();
	}
}
