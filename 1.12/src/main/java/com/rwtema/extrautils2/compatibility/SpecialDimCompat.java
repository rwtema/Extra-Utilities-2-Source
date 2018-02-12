package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.dimensions.workhousedim.WorldProviderSpecialDim;
import com.rwtema.extrautils2.utils.datastructures.FieldSetter;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class SpecialDimCompat {
	public static final FieldSetter<World, WorldProvider> providerFieldSetter = new FieldSetter<World, WorldProvider>(World.class, "field_73011_w", "provider");
	public static final FieldSetter<ChunkGeneratorEnd, World> endWorldFieldSetter = new FieldSetter<>(ChunkGeneratorEnd.class, "field_73230_p", "world", "worldObj");
	public static final FieldSetter<ChunkGeneratorHell, World> hellWorldFieldSetter = new FieldSetter<>(ChunkGeneratorHell.class, "field_185952_n", "world", "worldObj");
	public static ChunkGeneratorEnd tempEnd = null;
	public static ChunkGeneratorHell tempHell = null;
	public static WorldProviderHell providerHell = (WorldProviderHell) DimensionManager.createProviderFor(-1);
	public static WorldProviderEnd providerEnd = (WorldProviderEnd) DimensionManager.createProviderFor(1);

	@Nonnull
	public static Object getEndGenerator(final Biome targetBiome, final WorldServer world) {
		ChunkGeneratorEnd generator;
		generator = tempEnd;
		if (generator == null) {
			generator = CompatHelper.getChunkProviderEnd(world);
			tempEnd = generator;
		} else {
			endWorldFieldSetter.apply(generator, world);
		}
		providerEnd.setWorld(world);
		providerEnd.setDimension(1);
		providerFieldSetter.apply(world, providerEnd);
		return generator;
	}

	public static void clearEndData(WorldServer world, WorldProvider provider) {
		WorldProviderSpecialDim.isEnd = false;
		endWorldFieldSetter.apply(tempEnd, null);
		providerFieldSetter.apply(world, provider);

	}

	@Nonnull
	public static IChunkGenerator getNetherGen(final Biome targetBiome, final WorldServer world) {
		ChunkGeneratorHell generator;
		generator = tempHell;
		if (generator == null) {
			int seaLevel = world.getSeaLevel();
			generator = new ChunkGeneratorHell(world, true, world.getSeed()) {
				@Nonnull
				@Override
				public Chunk generateChunk(int x, int z) {
					Chunk chunk = super.generateChunk(x, z);
					for (ExtendedBlockStorage extendedBlockStorage : chunk.getBlockStorageArray()) {
						if (extendedBlockStorage != null) {
							if (extendedBlockStorage.getBlockLight() == null) {
								extendedBlockStorage.setBlockLight(new NibbleArray());
							}
						}
					}
					return chunk;
				}
			};
			world.setSeaLevel(seaLevel);
			tempHell = generator;
		} else {
			hellWorldFieldSetter.apply(generator, world);
		}

		providerHell.setWorld(world);
		providerHell.setDimension(-1);
		providerFieldSetter.apply(world, providerHell);
		return generator;
	}

	public static void clearNetherData(WorldServer world, WorldProvider provider) {
		WorldProviderSpecialDim.isNether = false;
		hellWorldFieldSetter.apply(tempHell, null);
		providerFieldSetter.apply(world, provider);

	}

	public static void populate(Chunk chunk, Object generator) {
		((IChunkGenerator) generator).populate(chunk.x, chunk.z);
		GameRegistry.generateWorld(chunk.x, chunk.z, chunk.getWorld(), ((IChunkGenerator) generator), chunk.getWorld().getChunkProvider());
	}
}
