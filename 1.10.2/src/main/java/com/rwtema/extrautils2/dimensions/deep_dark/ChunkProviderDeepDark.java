package com.rwtema.extrautils2.dimensions.deep_dark;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.ChunkGeneratorCompat;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class ChunkProviderDeepDark implements ChunkGeneratorCompat {
	public static final int FLOOR_HEIGHT = 62;
	public static final int CEILING_HEIGHT = 120;
	static ImmutableSet<OreGenEvent.GenerateMinable.EventType> bannedOres = ImmutableSet.of(
			OreGenEvent.GenerateMinable.EventType.DIRT,
			OreGenEvent.GenerateMinable.EventType.GRAVEL,
			OreGenEvent.GenerateMinable.EventType.ANDESITE,
			OreGenEvent.GenerateMinable.EventType.DIORITE,
			OreGenEvent.GenerateMinable.EventType.GRANITE
	);

	static {
		MinecraftForge.ORE_GEN_BUS.register(ChunkProviderDeepDark.class);
	}

	private final World world;
	private final List<MapGenStructure> structureGenerators = Lists.newArrayList();
	private final List<MapGenBase> generators = Lists.newArrayList();
	final ThreadLocal<Integer> seedOffset = ThreadLocal.withInitial(() -> 0);
	Random random = new Random();
	GenStates[] nextState;
	Biome[] biomes = null;
	private WorldGenLakes waterLakeGenerator;
	private WorldGenLakes lavaLakeGenerator;

	{
		int length = GenStates.values().length;
		nextState = new GenStates[length - 1];
		System.arraycopy(GenStates.values(), 1, nextState, 0, length - 1);
	}

	public ChunkProviderDeepDark(World worldIn, long seed) {
		world = worldIn;

		this.structureGenerators.add(new MapGenScatteredFeature());
		this.structureGenerators.add(new MapGenMineshaft());

		generators.add(TerrainGen.getModdedMapGen(new MapGenCaves(), InitMapGenEvent.EventType.CAVE));
		generators.add(TerrainGen.getModdedMapGen(new MapGenRavine(), InitMapGenEvent.EventType.RAVINE));

		this.waterLakeGenerator = new WorldGenLakes(Blocks.WATER);
		this.lavaLakeGenerator = new WorldGenLakes(Blocks.LAVA);

	}

	@SubscribeEvent
	public static void preventOres(OreGenEvent.GenerateMinable event) {
		OreGenEvent.GenerateMinable.EventType type = event.getType();
		if (bannedOres.contains(type) && event.getWorld().provider.getDimensionType() == XU2Entries.deep_dark.value) {
			event.setResult(Event.Result.DENY);
		}
	}

	@Nonnull
	@Override
	public Chunk generateChunk(int x, int z) {


		ChunkPrimer chunkprimer = new ChunkPrimer();

		random.setSeed(world.getSeed() + (x >> 2) * 65535 + (z >> 2));

		int spire_x = ((x >> 2) * 64) + (8 + random.nextInt(48)) - (x * 16);
		int spire_z = ((z >> 2) * 64) + (8 + random.nextInt(48)) - (z * 16);

		random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
		GenStates[] values = GenStates.values();
		for (int dx = 0; dx < 16; ++dx) {
			for (int dz = 0; dz < 16; ++dz) {
				int rs = (spire_x - dx) * (spire_x - dx) + (spire_z - dz) * (spire_z - dz);

				double spire_dist = rs < 256 ? Math.sqrt(rs) : Double.MAX_VALUE;

				GenStates curState = GenStates.FLOOR_BEDROCK;
				for (int dy = 0; dy < 256; dy++) {
					IBlockState state = curState.state;

					if (curState == GenStates.AIR) {
						if (rs < 256) {
							int m = Math.min(dy - FLOOR_HEIGHT, CEILING_HEIGHT - dy);
							double t = spire_dist;

							if (m < 9) {
								t -= Math.sqrt(9 - m);
							}

							if (t <= 4 || t <= 5 && random.nextBoolean()) {
								state = BlockStates.COBBLESTONE;
							}
						}
					}

					if (dy >= 253) {
						state = BlockStates.BEDROCK;
					}

					chunkprimer.setBlockState(dx, dy, dz, state);

					boolean advance;
					switch (curState) {
						case FLOOR_BEDROCK:
							advance = dy > 2 || dy > 0 && random.nextBoolean();
							break;
						case GROUND:
							advance = dy >= (FLOOR_HEIGHT + 2) || dy >= FLOOR_HEIGHT && random.nextInt(4) != 0;
							break;
						case AIR:
							advance = dy >= 90 && (dy >= CEILING_HEIGHT || random.nextInt(1 + 2 * (CEILING_HEIGHT - dy) * (CEILING_HEIGHT - dy)) == 0);
							break;
						case CEILING:
							advance = dy >= CEILING_HEIGHT && random.nextInt(40) == 0;
							break;
						case CEILING_STONE:
							advance = dy >= 253;

							break;
						case CEILING_BEDROCK:
							advance = false;
							break;
						default:
							throw new RuntimeException("Invalid State " + curState);
					}
					if (advance) {
						curState = values[curState.ordinal() + 1];
					}
				}
			}
		}

		for (MapGenBase generator : generators) {
			generator.generate(world, x, z, chunkprimer);
		}

		for (MapGenBase mapgenbase : this.structureGenerators) {
			mapgenbase.generate(this.world, x, z, chunkprimer);
		}

		for (int dx = 0; dx < 16; ++dx) {
			for (int dz = 0; dz < 16; ++dz) {
				for (int dy = FLOOR_HEIGHT; dy < FLOOR_HEIGHT + 3; dy++) {
					if (chunkprimer.getBlockState(dx, dy, dz) == BlockStates.STONE) {
						chunkprimer.setBlockState(dx, dy, dz, BlockStates.COBBLESTONE);
					}
				}
			}
		}

		Chunk chunk = new Chunk(this.world, chunkprimer, x, z);

		biomes = this.world.getBiomeProvider().getBiomesForGeneration(biomes, x * 16, z * 16, 16, 16);
		byte[] biomeIDs = chunk.getBiomeArray();

		for (int l = 0; l < biomeIDs.length; ++l) {
			biomeIDs[l] = (byte) Biome.getIdForBiome(biomes[l]);
		}

		chunk.generateSkylightMap();
		return chunk;
	}


	@Override
	public void populate(int x, int z) {
		BlockFalling.fallInstantly = true;
		int i = x * 16;
		int j = z * 16;
		BlockPos blockpos = new BlockPos(i, 0, j);
		Biome biome = this.world.getBiomeForCoordsBody(new BlockPos(i + 16, 0, j + 16));
		boolean flag = false;
		this.random.setSeed(this.world.getSeed());
		long k = this.random.nextLong() / 2L * 2L + 1L;
		long l = this.random.nextLong() / 2L * 2L + 1L;
		this.random.setSeed((long) x * k + (long) z * l ^ this.world.getSeed());
		ChunkPos chunkpos = new ChunkPos(x, z);

		ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, x, z, false);

		for (MapGenStructure mapgenstructure : this.structureGenerators) {
			boolean flag1 = mapgenstructure.generateStructure(this.world, this.random, chunkpos);

			if (mapgenstructure instanceof MapGenVillage) {
				flag |= flag1;
			}
		}

		if (this.waterLakeGenerator != null && !flag && this.random.nextInt(4) == 0) {
			this.waterLakeGenerator.generate(this.world, this.random, blockpos.add(this.random.nextInt(16) + 8, this.random.nextInt(256), this.random.nextInt(16) + 8));
		}

		if (this.lavaLakeGenerator != null && !flag && this.random.nextInt(8) == 0) {
			BlockPos blockpos1 = blockpos.add(this.random.nextInt(16) + 8, this.random.nextInt(this.random.nextInt(248) + 8), this.random.nextInt(16) + 8);

			if (blockpos1.getY() < this.world.getSeaLevel() || this.random.nextInt(10) == 0) {
				this.lavaLakeGenerator.generate(this.world, this.random, blockpos1);
			}
		}

		for (int i1 = 0; i1 < 8; ++i1) {
			(new WorldGenDungeons()).generate(this.world, this.random, blockpos.add(this.random.nextInt(16) + 8, this.random.nextInt(256), this.random.nextInt(16) + 8));
		}

		if(seedOffset.get() != 0){
			return;
		}

		seedOffset.set(1);
		biome.decorate(this.world, this.random, blockpos);
		ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, x, z, flag);
		BlockFalling.fallInstantly = false;
		GameRegistry.generateWorld(x, z, this.world, this, this.world.getChunkProvider());
		seedOffset.set(0);
	}

	@Override
	public boolean generateStructures(@Nonnull Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	@Nonnull
	public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
		Biome biome = this.world.getBiomeForCoordsBody(pos);
		return biome.getSpawnableList(creatureType);
	}




	@Override
	public void recreateStructures(@Nonnull Chunk chunkIn, int x, int z) {
		for (MapGenStructure mapgenstructure : this.structureGenerators) {
			//noinspection ConstantConditions
			mapgenstructure.generate(this.world, x, z, null);
		}
	}



	enum GenStates {
		FLOOR_BEDROCK(BlockStates.BEDROCK),
		GROUND(BlockStates.STONE),
		AIR(BlockStates.AIR),
		CEILING(BlockStates.COBBLESTONE),
		CEILING_STONE(BlockStates.STONE),
		CEILING_BEDROCK(BlockStates.BEDROCK);

		final IBlockState state;

		GenStates(IBlockState state) {
			this.state = state;
		}
	}
}
