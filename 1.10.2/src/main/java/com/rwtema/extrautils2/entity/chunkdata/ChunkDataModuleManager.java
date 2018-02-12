package com.rwtema.extrautils2.entity.chunkdata;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ChunkDataModuleManager<T> {

	public abstract T getCachedBlank();

	public abstract T createBlank();

	public boolean onUpdate(Chunk chunk, T t) {
		return false;
	}

	public abstract void writeToNBT(NBTTagCompound base, T t);

	public abstract T readFromNBT(NBTTagCompound tag);

	public abstract void writeData(T value, XUPacketBuffer buffer);

	public abstract void readData(T value, XUPacketBuffer buffer);

	@SideOnly(Side.CLIENT)
	public void clientTick(Chunk chunk, T t) {

	}
}
