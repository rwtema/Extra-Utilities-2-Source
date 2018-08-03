package com.rwtema.extrautils2.compatibility;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public interface IWorldGeneratorCompat extends IWorldGenerator {
	@Override
	default void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		gen(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
	}

	void gen(Random random, int chunkX, int chunkZ, World world, Object chunkGenerator, IChunkProvider chunkProvider);
}
