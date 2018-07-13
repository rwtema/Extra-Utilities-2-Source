package com.rwtema.extrautils2.dimensions.workhousedim;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.save.SaveManager;
import com.rwtema.extrautils2.backend.save.SaveModule;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.SpecialDimCompat;
import com.rwtema.extrautils2.dimensions.XUWorldProvider;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.NullHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class WorldProviderSpecialDim extends XUWorldProvider {
	public static final int DIST_BETWEEN_CHUNKS = 6;
	public static final NBTSerializable.NBTCollection<ChunkPos, Set<ChunkPos>, NBTTagLong> diggingChunks =
			new NBTSerializable.NBTCollection<>(new HashSet<>(),
					chunkPos -> new NBTTagLong(((long) chunkPos.x << 32) | (chunkPos.z & 0xFFFFFFFFL)),
					nbtTagLong -> {
						long c = nbtTagLong.getLong();
						return new ChunkPos((int) (c >> 32), (int) c);
					}
			);
	static final Set<BiomeDictionary.Type> blacklist = ImmutableSet.of(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.END);

	public static Biome biome = null;
	public static boolean isEnd = false;
	public static boolean isNether = false;
	public static Long seedOverride = null;
	public static boolean ALLOW_SPECIAL_DIMS;
	static List<Biome> suitableBiomes = null;
	static WorldProvider tempWorldHolder;
	static IBlockState BORDER_STATE = Blocks.BEDROCK.getDefaultState();
	private ChunkProviderSpecialDim chunkProviderSpecialDim;

	public WorldProviderSpecialDim() {
		super(XU2Entries.specialdim);
		this.nether = true;
		SaveManager.saveModules.add(new DigLocationsSaveModule());
		MinecraftForge.EVENT_BUS.register(WorldProviderSpecialDim.class);
	}

	@Nonnull
	private static List<Biome> buildBiomesList() {
		ArrayList<Biome> biomes = new ArrayList<>();
		mainLoop:
		for (Biome biome : Biome.REGISTRY) {
			for (BiomeDictionary.Type type : CompatHelper.getTypesForBiome(biome)) {
				if (blacklist.contains(type)) {
					continue mainLoop;
				}
			}
			biomes.add(biome);
		}

		return biomes;
	}

	@SubscribeEvent
	public static void preventTeleportation(EntityTravelToDimensionEvent event) {

	}

	@SubscribeEvent
	public static void onJoin(EntityJoinWorldEvent event) {
		if (isSpecialDim(event.getWorld()) && event.getEntity() instanceof EntityLiving) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void preventSpawn(LivingSpawnEvent.CheckSpawn event) {
		if (isSpecialDim(event.getWorld())) {
			event.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public static void preventSpawn(LivingSpawnEvent.SpecialSpawn event) {
		if (isSpecialDim(event.getWorld())) {
			event.setCanceled(true);
		}
	}

	private static boolean isSpecialDim(World world) {
		return world.provider instanceof WorldProviderSpecialDim && world.provider.getDimension() == XU2Entries.specialdim.id;
	}


	private static void resetChunkBlocks(World world, int x, int z) {
		Chunk chunk = world.getChunkFromChunkCoords(x, z);

		ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
		for (ClassInheritanceMultiMap<Entity> entityList : entityLists) {
			for (Entity entity : entityList) {
				if (!(entity instanceof EntityPlayer))
					entity.setDead();
			}
		}

		for (TileEntity tileEntity : CollectionHelper.wrapConcurrentErrorReport(chunk.getTileEntityMap().values())) {
			world.markTileEntityForRemoval(tileEntity);
		}

		chunk.getTileEntityMap().clear();

		WorldProviderSpecialDim provider = (WorldProviderSpecialDim) getWorld().provider;
		chunk.setStorageArrays(provider.chunkProviderSpecialDim.generateChunk(x, z).getBlockStorageArray());

		for (ExtendedBlockStorage extendedBlockStorage : chunk.getBlockStorageArray()) {
			if (extendedBlockStorage != null) {
				if (extendedBlockStorage.getBlockLight() == null) {
					extendedBlockStorage.setBlockLight(new NibbleArray());
				}
			}
		}

		chunk.setTerrainPopulated(true);
		chunk.markDirty();
	}

	public static void releaseChunk(ChunkPos pos) {
		synchronized (diggingChunks) {
			diggingChunks.collection.remove(pos);
			ChunkPos chunkPos = adjustChunkRef(pos);
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					resetChunkBlocks(getWorld(), chunkPos.x + dx, chunkPos.z + dz);
				}
			}
		}
	}

	public static ChunkPos prepareNewChunk(Biome targetBiome) {
		synchronized (diggingChunks) {
			ChunkPos pos = new ChunkPos(0, 0);

			int i = 0, n = 1;
			boolean z = false;
			Dir d = Dir.RIGHT;
			while (true) {
				if (diggingChunks.collection.contains(pos)) {
					pos = new ChunkPos(pos.x + d.dx, pos.z + d.dz);
					i++;
					if (i == n) {
						i = 0;
						if (z) n++;
						z = !z;
						d = d.getNext();
					}
				} else {
					break;
				}
			}

			diggingChunks.collection.add(pos);

			WorldServer world = getWorld();

			WorldProvider provider = world.provider;

			ChunkPos chunkPos = adjustChunkRef(pos);
			ChunkProviderServer chunkProvider = world.getChunkProvider();
			Object generator = chunkProvider.chunkGenerator;

			try {
				isNether = isEnd = false;

				if (ALLOW_SPECIAL_DIMS) {
					if (targetBiome != null) {
						if (CompatHelper.isBiomeOfType(targetBiome, BiomeDictionary.Type.END)) {
							generator = SpecialDimCompat.getEndGenerator(targetBiome, world);
							isEnd = true;
						} else if (CompatHelper.isBiomeOfType(targetBiome, BiomeDictionary.Type.NETHER)) {
							isNether = true;
							generator = SpecialDimCompat.getNetherGen(targetBiome, world);
						}
					}
				}

				seedOverride = XURandom.rand.nextLong();

				if (targetBiome == null) {
					if (suitableBiomes == null) suitableBiomes = buildBiomesList();
					biome = XURandom.getRandomElement(suitableBiomes);
				} else {
					biome = targetBiome;
				}

				if (isNether || isEnd) {
					for (int dx = -2; dx <= 2; dx++) {
						for (int dz = -2; dz <= 2; dz++) {
							Chunk chunk = chunkProvider.provideChunk(chunkPos.x + dx, chunkPos.z + dz);
							BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
							if (isEnd) {
								IBlockState end_stone = Blocks.END_STONE.getDefaultState();
								for (int ddx = 0; ddx < 16; ddx++) {
									for (int ddz = 0; ddz < 16; ddz++) {
										for (int ddy = 49 - 10; ddy < 49 + 11; ddy++) {
											blockPos.setPos(ddx, ddy, ddz);
											chunk.setBlockState(blockPos, end_stone);
										}
									}
								}
							} else if (isNether) {
								IBlockState lava = Blocks.LAVA.getDefaultState();
								IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
								IBlockState netherrack = Blocks.NETHERRACK.getDefaultState();
								IBlockState soulsand = Blocks.SOUL_SAND.getDefaultState();
								for (int ddx = 0; ddx < 16; ddx++) {
									for (int ddz = 0; ddz < 16; ddz++) {
										blockPos.setPos(ddx, 0, ddz);
										chunk.setBlockState(blockPos, bedrock);
										for (int dy = 1; dy < 20; dy++) {
											chunk.setBlockState(blockPos, netherrack);
										}
										IBlockState lava1;
										if (ddx == 0 || ddx == 15 || ddz == 0 || ddz == 15) {
											lava1 = lava;
										} else {
											lava1 = netherrack;
										}
										for (int dy = 20; dy < 30; dy++) {
											blockPos.setPos(ddx, dy, ddz);
											chunk.setBlockState(blockPos, lava1);
										}

										blockPos.setPos(ddx, 60, ddz);
										chunk.setBlockState(blockPos, netherrack);
										blockPos.setPos(ddx, 61, ddz);
										chunk.setBlockState(blockPos, soulsand);

										for (int dy = 90; dy < 255; dy++) {
											blockPos.setPos(ddx, dy, ddz);
											chunk.setBlockState(blockPos, netherrack);
										}
										blockPos.setPos(ddx, 255, ddz);
										chunk.setBlockState(blockPos, bedrock);
									}
								}

							}
						}
					}
				}

				generate(chunkPos.x, chunkPos.z, chunkProvider, generator);
				generate(chunkPos.x, chunkPos.z - 1, chunkProvider, generator);
				generate(chunkPos.x - 1, chunkPos.z, chunkProvider, generator);
				generate(chunkPos.x - 1, chunkPos.z - 1, chunkProvider, generator);

				if (isEnd || isNether) {
					int x0 = ((chunkPos.x - 1) << 4) - 1;
					int x1 = ((chunkPos.x - 1) << 4) + 16 * 2;
					int z0 = ((chunkPos.z - 1) << 4) - 1;
					int z1 = ((chunkPos.z - 1) << 4) + 16 * 2;
					addBorderColumnLine(world, x0, x0, z0, z1 - 1);
					addBorderColumnLine(world, x1, x1, z0, z1 - 1);
					addBorderColumnLine(world, x0, x1, z0, z0);
					addBorderColumnLine(world, x0, x1, z1, z1);
				}

			} catch (Exception e) {
				if (isEnd) {
					throw new RuntimeException("Error while simulating End dimension with Quantum Quarry", e);
				}
				if (isNether) {
					throw new RuntimeException("Error while simulating Nether dimension with Quantum Quarry", e);
				}
				throw Throwables.propagate(e);
			} finally {
				seedOverride = null;
				biome = null;
				if (isEnd) {
					SpecialDimCompat.clearEndData(world, provider);
				}
				if (isNether) {
					SpecialDimCompat.clearNetherData(world, provider);
				}
			}

			return pos;
		}
	}

	public static void addBorderColumn(World world, BlockPos.MutableBlockPos pos) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		for (int i = 0; i < 256; i++) {
			pos.setY(i);
			chunk.setBlockState(pos, BORDER_STATE);
		}
	}

	public static void addBorderColumnLine(World world, int x0, int x1, int z0, int z1) {
		for (BlockPos.MutableBlockPos pos : BlockPos.MutableBlockPos.getAllInBoxMutable(new BlockPos(x0, 0, z0), new BlockPos(x1, 0, z1))) {
			addBorderColumn(world, pos);
			pos.setY(0);
		}
	}

	private static void generate(int x, int z, ChunkProviderServer chunkProvider, Object generator) {
		Chunk chunk = chunkProvider.provideChunk(x, z);

		for (ExtendedBlockStorage extendedBlockStorage : chunk.getBlockStorageArray()) {
			if (extendedBlockStorage != null && NullHelper.nullable(extendedBlockStorage.getBlockLight()) == null) {
				extendedBlockStorage.setBlockLight(new NibbleArray());
			}

			if (extendedBlockStorage != null && NullHelper.nullable(extendedBlockStorage.getSkyLight()) == null) {
				extendedBlockStorage.setSkyLight(new NibbleArray());
			}
		}

		int idForBiome = Biome.getIdForBiome(biome);
		Arrays.fill(chunk.getBiomeArray(), (byte) idForBiome);
		chunk.setTerrainPopulated(true);
		SpecialDimCompat.populate(chunk, generator);

		chunk.markDirty();
		chunk.setTerrainPopulated(true);

		for (ExtendedBlockStorage extendedBlockStorage : chunk.getBlockStorageArray()) {
			if (extendedBlockStorage != null) {
				if (NullHelper.nullable(extendedBlockStorage.getBlockLight()) == null) {
					extendedBlockStorage.setBlockLight(new NibbleArray());
				}
				if (NullHelper.nullable(extendedBlockStorage.getSkyLight()) == null) {
					extendedBlockStorage.setSkyLight(new NibbleArray());
				}
			}
		}
	}

	public static WorldServer getWorld() {
		return DimensionManager.getWorld(XU2Entries.specialdim.value.getId());
	}


	public static ChunkPos adjustChunkRef(ChunkPos pos) {
		return new ChunkPos(pos.x * DIST_BETWEEN_CHUNKS, pos.z * DIST_BETWEEN_CHUNKS);
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	@Nonnull
	public ChunkProviderSpecialDim createChunkGenerator() {
		chunkProviderSpecialDim = new ChunkProviderSpecialDim(world);
		return chunkProviderSpecialDim;
	}

	@Override
	@Nonnull
	public Biome getBiomeForCoords(@Nonnull BlockPos pos) {
		if (biome != null) return biome;
		return super.getBiomeForCoords(pos);
	}

	@Override
	public long getSeed() {
		Long seedOverride = WorldProviderSpecialDim.seedOverride;
		if (seedOverride != null) return seedOverride;
		return super.getSeed();
	}

	enum Dir {
		RIGHT(1, 0),
		UP(0, 1),
		LEFT(-1, 0),
		DOWN(0, -1);
		final int dx;
		final int dz;

		Dir next;

		Dir(int dx, int dz) {
			this.dx = dx;
			this.dz = dz;
		}


		public Dir getNext() {
			if (next == null) {
				next = values()[(ordinal() + 1) % 4];
			}
			return next;
		}
	}

	private static class DigLocationsSaveModule extends SaveModule {

		public DigLocationsSaveModule() {
			super("special_dim_dig_s");
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			synchronized (diggingChunks) {
				diggingChunks.deserializeNBT(nbt.getTagList("s", Constants.NBT.TAG_LONG));
			}
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			synchronized (diggingChunks) {
				nbt.setTag("s", diggingChunks.serializeNBT());
			}
		}

		@Override
		public void reset() {
			diggingChunks.collection.clear();
		}
	}


}
