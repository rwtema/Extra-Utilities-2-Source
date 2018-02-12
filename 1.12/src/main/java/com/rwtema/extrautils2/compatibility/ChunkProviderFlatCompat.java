package com.rwtema.extrautils2.compatibility;

import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorFlat;

public class ChunkProviderFlatCompat extends ChunkGeneratorFlat {
	public ChunkProviderFlatCompat(World worldIn, long seed, boolean generateStructures, String flatGeneratorSettings) {
		super(worldIn, seed, generateStructures, flatGeneratorSettings);
	}
}
