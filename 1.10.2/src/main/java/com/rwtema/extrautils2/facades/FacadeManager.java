package com.rwtema.extrautils2.facades;

import com.google.common.collect.Multimap;
import com.rwtema.extrautils2.entity.chunkdata.ChunkDataModuleManager;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class FacadeManager extends ChunkDataModuleManager<Multimap<BlockPos, Facade>> {
	@Override
	public Multimap<BlockPos, Facade> getCachedBlank() {
		return null;
	}

	@Override
	public Multimap<BlockPos, Facade> createBlank() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound base, Multimap<BlockPos, Facade> blockPosFacadeMultimap) {

	}

	@Override
	public Multimap<BlockPos, Facade> readFromNBT(NBTTagCompound tag) {
		return null;
	}

	@Override
	public void writeData(Multimap<BlockPos, Facade> value, XUPacketBuffer buffer) {

	}

	@Override
	public void readData(Multimap<BlockPos, Facade> value, XUPacketBuffer buffer) {

	}
}
