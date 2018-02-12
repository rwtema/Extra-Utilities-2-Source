package com.rwtema.extrautils2.backend.save;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SaveModule {
	String name;

	public SaveModule(String name) {
		this.name = name;
	}

	public abstract void readFromNBT(NBTTagCompound nbt);

	public abstract void writeToNBT(NBTTagCompound nbt);

	public void markDirty() {
		if (SaveManager.manager != null)
			SaveManager.manager.markDirty();
	}

	public abstract void reset();
}
