package com.rwtema.extrautils2.compatibility;

import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderFlat;

public class ChunkProviderFlatCompat extends ChunkProviderFlat {
	public ChunkProviderFlatCompat(World worldIn, long seed, boolean generateStructures, String flatGeneratorSettings) {
		super(worldIn, seed, generateStructures, flatGeneratorSettings);
	}
}
