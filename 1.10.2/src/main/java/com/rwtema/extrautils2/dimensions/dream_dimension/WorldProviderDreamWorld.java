package com.rwtema.extrautils2.dimensions.dream_dimension;

import com.rwtema.extrautils2.dimensions.XUWorldProvider;

import javax.annotation.Nonnull;

public class WorldProviderDreamWorld extends XUWorldProvider {
	public WorldProviderDreamWorld() {
//		super(XU2Entries.dream_world);
		super(null);
	}

	@Override
	public int getAverageGroundLevel() {
		return ChunkProviderDream.GROUND_HEIGHT;
	}

	@Nonnull
	@Override
	public ChunkProviderDream createChunkGenerator() {
		return new ChunkProviderDream(world);
	}

}
