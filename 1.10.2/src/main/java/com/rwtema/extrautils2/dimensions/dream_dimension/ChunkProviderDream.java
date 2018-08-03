package com.rwtema.extrautils2.dimensions.dream_dimension;

import com.rwtema.extrautils2.compatibility.ChunkGeneratorCompat;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChunkProviderDream implements ChunkGeneratorCompat {
	static final int base = (1 << 25) - 1;
	static final int GROUND_HEIGHT = 16;

	private final World world;
	Random rand = new Random();

	NoiseGeneratorOctaves depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
	double[] depthRegion;
	private NoiseGeneratorPerlin surfaceNoise = new NoiseGeneratorPerlin(this.rand, 4);

	public ChunkProviderDream(World worldObj) {
		this.world = worldObj;
	}

	public boolean getValue(int x, int y) {
		int r = base;
		x = x % r;
		y = y % r;
		while (r > 0) {
			if (x == 0) {
				return y == 1;
			} else if (y == 0) {
				return x == r || x == -r;
			} else {
				r = (r - 1) >> 1;
				int rc = r + 1;
				if (y > 0) {
					if (x < 0) {
						x = -x - rc;
					} else {
						x = x - rc;
					}
					y = y - rc;
				} else {
					int x0 = x, y0 = y;
					if (x < 0) {
						x = -y0 - rc;
						y = x0 + rc;
					} else {
						y = -x0 + rc;
						x = y0 + rc;
					}
				}
			}
		}
		return true;
	}

	@Override
	@Nonnull
	public Chunk generateChunk(int chunk_x, int chunk_z) {
		ChunkPrimer chunkprimer = new ChunkPrimer();
		rand.setSeed((long) chunk_x * 341873128712L + (long) chunk_z * 132897987541L);

//		depthRegion = this.depthNoise.generateNoiseOctaves(
//				this.depthRegion,
//				chunk_x * 16, chunk_z * 16,
//				17, 17,
//				1,
//				1,
//				0);

		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = (chunk_x << 4) + dx;
				int z = (chunk_z << 4) + dz;

				boolean value = getValue(x, z);
				IBlockState state = value ? BlockStates.RED_SANDSTONE : BlockStates.SANDSTONE;

				for (int y = 0; y <= GROUND_HEIGHT; y++) {
					chunkprimer.setBlockState(dx, y, dz, y <= 1 ? BlockStates.BEDROCK : state);
				}
			}
		}

		Chunk chunk = new Chunk(this.world, chunkprimer, chunk_x, chunk_z);

		byte b = (byte) Biome.getIdForBiome(Biomes.DESERT);
		byte[] biomeIDs = chunk.getBiomeArray();
		for (int l = 0; l < biomeIDs.length; ++l) {
			biomeIDs[l] = b;
		}
		chunk.generateSkylightMap();

		return chunk;
	}

	@Override
	public void populate(int x, int z) {
		BlockFalling.fallInstantly = true;
		this.rand.setSeed(this.world.getSeed());
		long k = this.rand.nextLong() / 2L * 2L + 1L;
		long l = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed((long) x * k + (long) z * l ^ this.world.getSeed());
		ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);
		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		Biome biome = this.world.getBiome(pos);
		return biome.getSpawnableList(creatureType);
	}

	@Nullable
	@Override
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {

	}


	private void generateHeightmap(int p_185978_1_, int p_185978_2_, int p_185978_3_) {


	}
}
