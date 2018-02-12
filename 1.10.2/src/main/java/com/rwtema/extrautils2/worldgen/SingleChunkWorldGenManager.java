package com.rwtema.extrautils2.worldgen;

import com.rwtema.extrautils2.compatibility.IWorldGeneratorCompat;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SingleChunkWorldGenManager implements IWorldGeneratorCompat {
	public static SingleChunkWorldGenManager INSTANCE = new SingleChunkWorldGenManager();
	public static ThreadLocal<Boolean> isRetrogen = new ThreadLocalBoolean(false);
	List<SingleChunkGen> chunkGens = new ArrayList<>();

	private SingleChunkWorldGenManager() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void register(SingleChunkGen singleChunkGen) {
		INSTANCE.chunkGens.add(singleChunkGen);
	}

	@Override
	public void gen(Random random, int chunkX, int chunkZ, World world, Object chunkGenerator, IChunkProvider chunkProvider) {
		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		isRetrogen.set(false);
		for (SingleChunkGen chunkGen : chunkGens) {
			chunkGen.genChunk(chunk, chunkGenerator, random);
		}
	}

	@SubscribeEvent
	public void saveChunk(ChunkDataEvent.Save event) {
		if (!event.getChunk().isTerrainPopulated())
			return;

		NBTTagCompound data = event.getData();
		NBTTagCompound tag = NBTHelper.getOrInitTagCompound(data, "XU2Generation");

		for (SingleChunkGen chunkGen : chunkGens) {
			tag.setInteger(chunkGen.name, chunkGen.version);
		}
	}

	@SubscribeEvent
	public void loadChunk(ChunkDataEvent.Load event) {
		World world = event.getWorld();
		if (world.isRemote) return;
		long chunkSeed = -1;
		Chunk chunk = event.getChunk();
		Random random = null;
		Object chunkProvider = null;
		NBTTagCompound data = event.getData();
		NBTTagCompound tag = NBTHelper.getOrInitTagCompound(data, "XU2Generation");

		for (SingleChunkGen chunkGen : chunkGens) {
			int v;
			if (chunkGen == null ||
					(tag.hasKey(chunkGen.name, Constants.NBT.TAG_INT) &&
							((v = tag.getInteger(chunkGen.name)) == chunkGen.version || !chunkGen.shouldRegenOldVersion(v))))
				continue;


			if (chunkSeed == -1) {
				long worldSeed = world.getSeed();

				random = new Random(worldSeed);
				long xSeed = random.nextLong() >> 2 + 1L;
				long zSeed = random.nextLong() >> 2 + 1L;

				chunkSeed = (xSeed * chunk.x + zSeed * chunk.z) ^ worldSeed;
				chunkProvider = ((WorldServer) world).getChunkProvider().chunkGenerator;
			}


			isRetrogen.set(true);
			random.setSeed(chunkSeed);
			chunkGen.genChunk(chunk, chunkProvider, random);
		}
	}
}
