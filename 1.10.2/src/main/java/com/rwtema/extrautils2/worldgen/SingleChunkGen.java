package com.rwtema.extrautils2.worldgen;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Random;

public abstract class SingleChunkGen {

	static ThreadLocal<BlockPos.MutableBlockPos> mutableBlockPos = ThreadLocal.withInitial(() -> new BlockPos.MutableBlockPos());
	static IBlockState air = Blocks.AIR.getDefaultState();
	public final int version;
	public final String name;

	protected SingleChunkGen(String name, int version) {
		this.name = name;
		this.version = version;
	}

	public abstract void genChunk(Chunk chunk, Object provider, Random random);

	public boolean shouldRegenOldVersion(int v) {
		return true;
	}

	public boolean isAir(Chunk chunk, BlockPos pos) {
		return chunk.getBlockState(pos) == air;
	}

	public boolean isAir(Chunk chunk, BlockPos pos, EnumFacing side) {
		return isAir(chunk, pos.getX() + side.getFrontOffsetX(), pos.getY() + side.getFrontOffsetY(), pos.getZ() + side.getFrontOffsetZ());
	}


	public boolean isAir(Chunk chunk, int dx, int dy, int dz) {
		BlockPos.MutableBlockPos pos = SingleChunkGen.mutableBlockPos.get();
		pos.setPos(dx, dy, dz);
		return isAir(chunk, pos);
	}


	public void report(Chunk chunk, int dx, int dy, int dz) {
		if (!ExtraUtils2.deobf_folder) return;
		LogHelper.debug(name + " " + new BlockPos((chunk.x << 4) + dx, dy, (chunk.z << 4) + dz));
	}

	public void setBlockState(Chunk chunk, BlockPos pos, IBlockState state) {
		if (SingleChunkWorldGenManager.isRetrogen.get()) {
			int i = pos.getX() & 15;
			int j = pos.getY();
			int k = pos.getZ() & 15;
			int l = k << 4 | i;
			ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
			ExtendedBlockStorage extendedblockstorage = storageArrays[j >> 4];
			Block block = state.getBlock();
			if (extendedblockstorage == null) {
				if (block == Blocks.AIR) return;
				extendedblockstorage = storageArrays[j >> 4] = new ExtendedBlockStorage(j & (~15), true);
			}

			extendedblockstorage.set(i, j & 15, k, state);
		} else {
			chunk.setBlockState(pos, state);
		}
	}

}
