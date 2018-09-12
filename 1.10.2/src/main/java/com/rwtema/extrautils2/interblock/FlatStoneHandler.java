package com.rwtema.extrautils2.interblock;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.rwtema.extrautils2.entity.chunkdata.ChunkDataModuleManager;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public class FlatStoneHandler extends ChunkDataModuleManager<List<FlatStoneHandler.FlatStone>> {


	@Override
	public List<FlatStone> getCachedBlank() {
		return null;
	}

	@Override
	public List<FlatStone> createBlank() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound base, List<FlatStone> flatStones) {

	}

	@Override
	public List<FlatStone> readFromNBT(NBTTagCompound tag) {
		return null;
	}

	@Override
	public void writeData(List<FlatStone> value, XUPacketBuffer buffer) {

	}

	@Override
	public void readData(List<FlatStone> value, XUPacketBuffer buffer) {

	}

	public class FlatStone {
	}
}
