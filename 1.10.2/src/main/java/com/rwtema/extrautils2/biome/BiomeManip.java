package com.rwtema.extrautils2.biome;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BiomeManip {

	public static final Map<BiomeDictionary.Type, ItemRef> types;

	static {
		types = ImmutableMap.<BiomeDictionary.Type, ItemRef>builder()
				.put(BiomeDictionary.Type.END, ItemRef.wrap(Items.ENDER_PEARL))
				.put(BiomeDictionary.Type.NETHER, ItemRef.wrap(Items.NETHER_WART))
				.build();

	}

	public static void setMultiBiome(World world, Biome biome, BlockPos... poses) {
		byte id = (byte) Biome.getIdForBiome(biome);
		HashMultimap<ChunkPos, BlockPos> changes = HashMultimap.create();
		for (BlockPos pos : poses) {
			changes.put(new ChunkPos(pos), pos);
		}

		for (ChunkPos chunkPos : changes.keySet()) {
			Chunk chunk = world.getChunkFromChunkCoords(chunkPos.x, chunkPos.z);
			byte[] biomeArray = chunk.getBiomeArray();
			Set<BlockPos> changeSet = changes.get(chunkPos);
			for (Iterator<BlockPos> iterator = changeSet.iterator(); iterator.hasNext(); ) {
				BlockPos pos = iterator.next();
				int i = pos.getX() & 15;
				int j = pos.getZ() & 15;
				if (biomeArray[j << 4 | i] == id) {
					iterator.remove();
				} else {
					biomeArray[j << 4 | i] = id;
				}
			}
		}

		if (world instanceof WorldServer) {
			PlayerChunkMap playerChunkMap = ((WorldServer) world).getPlayerChunkMap();
			for (ChunkPos chunkPos : changes.keySet()) {
				Set<BlockPos> changeSet = changes.get(chunkPos);
				if (changeSet.isEmpty()) continue;

				PlayerChunkMapEntry entry = playerChunkMap.getEntry(chunkPos.x, chunkPos.z);
				if (entry != null) {
					entry.sendPacket(NetworkHandler.channels.get(Side.SERVER).generatePacketFrom(new PacketBiomeChange(biome, changeSet.toArray(new BlockPos[0]))));
				}
			}
		}
	}

	public static void setBiome(World world, Biome biome, BlockPos pos) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);

		int i = pos.getX() & 15;
		int j = pos.getZ() & 15;

		byte id = (byte) Biome.getIdForBiome(biome);

		byte b = chunk.getBiomeArray()[j << 4 | i];

		if (b == id) return;

		chunk.getBiomeArray()[j << 4 | i] = id;
		chunk.markDirty();

		if (world instanceof WorldServer) {
			PlayerChunkMap playerChunkMap = ((WorldServer) world).getPlayerChunkMap();
			int chunkX = pos.getX() >> 4;
			int chunkZ = pos.getZ() >> 4;

			PlayerChunkMapEntry entry = playerChunkMap.getEntry(chunkX, chunkZ);
			if (entry != null) {
				entry.sendPacket(NetworkHandler.channels.get(Side.SERVER).generatePacketFrom(new PacketBiomeChange(world, pos)));
			}
		}
	}

	@NetworkHandler.XUPacket
	public static class PacketBiomeChange extends XUPacketServerToClient {
		BlockPos[] positions;
		Biome biome;

		public PacketBiomeChange(World world, BlockPos pos) {
			this(world.getBiomeForCoordsBody(pos), pos);
		}

		public PacketBiomeChange(Biome biome, BlockPos... positions) {
			this.positions = positions;
			this.biome = biome;
		}

		public PacketBiomeChange() {

		}


		@Override
		public void writeData() throws Exception {
			writeByte(Biome.getIdForBiome(biome));
			writeShort(positions.length);
			for (BlockPos position : positions) {
				writeBlockPos(position);
			}
		}

		@Override
		public void readData(EntityPlayer player) {
			biome = Biome.getBiome(readUnsignedByte());
			int len = readShort();
			positions = new BlockPos[len];
			for (int i = 0; i < len; i++) {
				positions[i] = readBlockPos();
			}
		}

		@Override
		public Runnable doStuffClient() {
			return new RunnableClient() {
				@Override
				public void run() {
					WorldClient theWorld = Minecraft.getMinecraft().world;
					BiomeManip.setMultiBiome(theWorld, biome, positions);

					HashSet<ChunkPos> finishedPos = new HashSet<>();
					for (BlockPos pos : positions) {
						if (finishedPos.add(new ChunkPos(pos))) {
							int chunkX = pos.getX() >> 4;
							int chunkZ = pos.getZ() >> 4;

							theWorld.markBlockRangeForRenderUpdate(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 256, (chunkZ << 4) + 15);
						}
					}
				}
			};
		}
	}
}
