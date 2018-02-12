package com.rwtema.extrautils2.dimensions.workhousedim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.rwtema.extrautils2.compatibility.ChunkProviderFlatCompat;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;

import javax.annotation.Nonnull;
import java.util.List;

public class ChunkProviderSpecialDim extends ChunkProviderFlatCompat {
	public ChunkProviderSpecialDim(World worldIn) {
		super(worldIn, 0, true, getDefaultInfo());
	}

	public static String getDefaultInfo() {
		FlatGeneratorInfo flatgeneratorinfo = getDefaultFlatInfo();
		return flatgeneratorinfo.toString();
	}

	@Nonnull
	public static FlatGeneratorInfo getDefaultFlatInfo() {
		FlatGeneratorInfo flatgeneratorinfo = new FlatGeneratorInfo();
		flatgeneratorinfo.setBiome(Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES));
		flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(1, Blocks.BEDROCK));
		flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(60, Blocks.STONE));
		flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(3, Blocks.DIRT));
		flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(1, Blocks.GRASS));
		flatgeneratorinfo.getWorldFeatures().put("dungeon", Maps.newHashMap());
		flatgeneratorinfo.getWorldFeatures().put("decoration", Maps.newHashMap());
		flatgeneratorinfo.updateLayers();
		return flatgeneratorinfo;
	}

	@Override
	@Nonnull
	public Chunk generateChunk(int x, int z) {
		Chunk chunk = super.generateChunk(x, z);
		chunk.setTerrainPopulated(true);
		return chunk;
	}

	@Override
	@Nonnull
	public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
		return ImmutableList.of();
	}
}
