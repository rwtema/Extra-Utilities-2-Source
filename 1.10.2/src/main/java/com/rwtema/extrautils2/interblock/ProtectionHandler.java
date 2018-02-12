package com.rwtema.extrautils2.interblock;

import com.rwtema.extrautils2.entity.chunkdata.ChunkDataModuleManager;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.nbt.NBTTagCompound;

public class ProtectionHandler extends ChunkDataModuleManager<ProtectionHandler.ProtectionData> {


	@Override
	public ProtectionData getCachedBlank() {
		return null;
	}

	@Override
	public ProtectionData createBlank() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound base, ProtectionData protectionData) {

	}

	@Override
	public ProtectionData readFromNBT(NBTTagCompound tag) {
		return null;
	}

	@Override
	public void writeData(ProtectionData value, XUPacketBuffer buffer) {

	}

	@Override
	public void readData(ProtectionData value, XUPacketBuffer buffer) {


	}

	public static class ProtectionData {

	}

}
