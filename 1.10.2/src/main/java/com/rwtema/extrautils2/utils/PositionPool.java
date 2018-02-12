package com.rwtema.extrautils2.utils;

import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class PositionPool {

	public static final BlockPos MID_HEIGHT = new BlockPos(0, 128, 0);

	TLongObjectHashMap<BlockPos> pool = new TLongObjectHashMap<>();

	int numDuplicateLookups;

	public BlockPos getPos(int x, int y, int z) {
		long key = (128L + y) | ((z + 33554432L) << 9L) | ((x + 33554432L) << 37L);

		BlockPos blockPos = pool.get(key);
		if (blockPos != null) {
			numDuplicateLookups++;
			return blockPos;
		}

		blockPos = new BlockPos(x, y, z);
		pool.put(key, blockPos);
		return blockPos;
	}


	public BlockPos add(BlockPos input, int x, int y, int z) {
		return x == 0 && y == 0 && z == 0 ? input : getPos(input.getX() + x, input.getY() + y, input.getZ() + z);
	}

	public BlockPos add(BlockPos input, Vec3i vec) {
		return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? input : getPos(input.getX() + vec.getX(), input.getY() + vec.getY(), input.getZ() + vec.getZ());
	}

	public BlockPos subtract(BlockPos input, Vec3i vec) {
		return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? input : getPos(input.getX() - vec.getX(), input.getY() - vec.getY(), input.getZ() - vec.getZ());
	}

	public BlockPos subtract(BlockPos input, int x, int y, int z) {
		return x == 0 && y == 0 && z == 0 ? input : getPos(input.getX() - x, input.getY() - y, input.getZ() - z);
	}

	public BlockPos offset(BlockPos input, EnumFacing facing) {
		return getPos(input.getX() + facing.getFrontOffsetX(), input.getY() + facing.getFrontOffsetY(), input.getZ() + facing.getFrontOffsetZ());
	}

	public BlockPos offset(BlockPos input, EnumFacing facing, int n) {
		return n == 0 ? input : getPos(input.getX() + facing.getFrontOffsetX() * n, input.getY() + facing.getFrontOffsetY() * n, input.getZ() + facing.getFrontOffsetZ() * n);
	}

	public BlockPos intern(BlockPos pos) {
		return getPos(pos.getX(), pos.getY(), pos.getZ());
	}

	public void clear() {
		pool.clear();
		numDuplicateLookups = 0;
	}

	public int size() {
		return pool.size();
	}

	public int getNumDuplicateLookups() {
		return numDuplicateLookups;
	}
}
