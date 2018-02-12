package com.rwtema.extrautils2.compatibility;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChunkGeneratorCompat extends IChunkGenerator {
	@Nullable
	default BlockPos getStrongholdGen(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position) {
		return null;
	}

	@Nullable
	default BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
		return null;
	}

	@Nullable
	default BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
		return null;
	}

	@Nullable
	default BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position) {
		return null;
	}


	default boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
		return false;
	}
}
