package com.rwtema.extrautils2.chunkloading;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.tile.TileChunkLoader;
import com.rwtema.extrautils2.utils.helpers.DescribeHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import gnu.trove.list.array.TIntArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class XUChunkLoaderManager implements ForgeChunkManager.LoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {
	public final static HashMultimap<World, TileChunkLoader> chunkLoaders = HashMultimap.create();

	public static XUChunkLoaderManager instance = new XUChunkLoaderManager();
	public static boolean dirty = false;
	private static WeakHashMap<World, Void> worldsLoaded = new WeakHashMap<>();
	private static HashMap<World, HashMap<GameProfile, ForgeChunkManager.Ticket>> playerTickets = new HashMap<>();

	public static void init() {
		ForgeChunkManager.setForcedChunkLoadingCallback(ExtraUtils2.instance, instance);
		MinecraftForge.EVENT_BUS.register(instance);
	}

	public static void register(TileChunkLoader loader) {
		synchronized (chunkLoaders) {
			GameProfile profile = loader.getProfile();
			World world = loader.world();
			if (profile != null) {
				if (ForgeChunkManager.getPersistentChunksFor(world) != ImmutableSetMultimap.<ChunkPos, ForgeChunkManager.Ticket>of()) {
					ForgeChunkManager.Ticket playerTicket = instance.getPlayerTicket(profile, world);
					if (playerTicket != null) {
						ForgeChunkManager.forceChunk(playerTicket, new ChunkPos(loader.getPos().getX() >> 4, loader.getPos().getZ() >> 4));
					}
				}
			}
			chunkLoaders.put(world, loader);
			dirty = true;
		}
	}

	public static void unregister(TileChunkLoader loader) {
		synchronized (chunkLoaders) {
			chunkLoaders.remove(loader.world(), loader);
			dirty = true;
		}
	}

	public static void clear() {
		chunkLoaders.clear();
		playerTickets.clear();
		ChunkLoaderLoginTimes.instance.loaded = false;
	}

	@SubscribeEvent
	public void serverTick(TickEvent.ServerTickEvent event) {
		if (dirty) {
			reloadChunkLoaders();
		}
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void worldLoad(WorldEvent.Load event) {
		if (!worldsLoaded.containsKey(event.getWorld())) {
			worldsLoaded.put(event.getWorld(), null);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void worldLoad(WorldEvent.Unload event) {
		if (!worldsLoaded.containsKey(event.getWorld())) {
			worldsLoaded.remove(event.getWorld());
		}
	}


	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		playerTickets.remove(event.getWorld());
		chunkLoaders.removeAll(event.getWorld());
	}


	public void reloadChunkLoaders() {
		synchronized (chunkLoaders) {
			dirty = false;

			HashMultimap<World, ChunkPos> worldChunks = HashMultimap.create();
			Multimap<ForgeChunkManager.Ticket, ChunkPos> toUnload = HashMultimap.create();
			Multimap<ForgeChunkManager.Ticket, ChunkPos> loaded = HashMultimap.create();
			Multimap<ForgeChunkManager.Ticket, ChunkPos> toAdd = HashMultimap.create();

			for (HashMap<GameProfile, ForgeChunkManager.Ticket> map : playerTickets.values()) {
				for (ForgeChunkManager.Ticket ticket : map.values()) {
					ImmutableSet<ChunkPos> chunkList = ticket.getChunkList();
					for (ChunkPos pair : chunkList) {
						ticket.world.getBlockState(CompatHelper.getCenterBlock(pair, 20));

					}
					worldChunks.putAll(ticket.world, chunkList);
					toUnload.putAll(ticket, chunkList);
					loaded.putAll(ticket, chunkList);
				}
			}

			for (Iterator<TileChunkLoader> iterator = chunkLoaders.values().iterator(); iterator.hasNext(); ) {
				TileChunkLoader chunkLoader = iterator.next();
				if (chunkLoader.isInvalid()) {
					dirty = true;
					iterator.remove();
				}

				if (!chunkLoader.isLoaded()) {
					dirty = true;
					continue;
				}

				World world = chunkLoader.world();
				if (world == null || DimensionManager.getWorld(world.provider.getDimension()) != world
						|| !worldsLoaded.containsKey(world)
						) {
					dirty = true;
					continue;
				}

				GameProfile profile = chunkLoader.getProfile();
				if (chunkLoader.active && profile != null && ChunkLoaderLoginTimes.instance.isValid(profile)) {
					ForgeChunkManager.Ticket ticket = getPlayerTicket(profile, world);
					if (ticket != null)
						for (ChunkPos coordIntPair : chunkLoader.getChunkCoords()) {
							worldChunks.remove(world, coordIntPair);
							toUnload.remove(ticket, coordIntPair);
							if (!loaded.containsEntry(ticket, coordIntPair))
								toAdd.put(ticket, coordIntPair);
						}
				}
			}

			for (HashMap<GameProfile, ForgeChunkManager.Ticket> map : playerTickets.values()) {
				for (Iterator<ForgeChunkManager.Ticket> iterator = map.values().iterator(); iterator.hasNext(); ) {
					ForgeChunkManager.Ticket ticket = iterator.next();
					for (ChunkPos pair : toUnload.get(ticket)) {
						ForgeChunkManager.unforceChunk(ticket, pair);
					}
					for (ChunkPos pair : toAdd.get(ticket)) {
						ForgeChunkManager.forceChunk(ticket, pair);
					}
					if (ticket.getChunkList().isEmpty()) {
						ForgeChunkManager.releaseTicket(ticket);
						iterator.remove();
					} else {
						TIntArrayList x = new TIntArrayList();
						TIntArrayList z = new TIntArrayList();
						for (ChunkPos ChunkPos : ticket.getChunkList()) {
							x.add(ChunkPos.x);
							z.add(ChunkPos.z);
						}
						ticket.getModData().setIntArray("x", x.toArray());
						ticket.getModData().setIntArray("z", z.toArray());
					}
				}
			}

		}
	}


	public ForgeChunkManager.Ticket getPlayerTicket(GameProfile profile, World world) {
		HashMap<GameProfile, ForgeChunkManager.Ticket> gameProfileTicketHashMap = playerTickets.get(world);
		if (gameProfileTicketHashMap == null)
			playerTickets.put(world, gameProfileTicketHashMap = new HashMap<>());

		ForgeChunkManager.Ticket ticket = gameProfileTicketHashMap.get(profile);

		if (ticket == null) {
			ticket = ForgeChunkManager.requestPlayerTicket(ExtraUtils2.instance, profile.getName(), world, ForgeChunkManager.Type.NORMAL);
			NBTTagCompound tag = ticket.getModData();
			tag.setString("Name", profile.getName());
			UUID id = profile.getId();
			if (id != null) {
				tag.setLong("UUIDL", id.getLeastSignificantBits());
				tag.setLong("UUIDU", id.getMostSignificantBits());
			}

			gameProfileTicketHashMap.put(profile, ticket);
		}
		return ticket;
	}


	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
		dirty = true;
		HashMap<GameProfile, ForgeChunkManager.Ticket> cache = new HashMap<>();
		playerTickets.put(world, cache);
		for (ForgeChunkManager.Ticket ticket : tickets) {
			NBTTagCompound modData = ticket.getModData();
			GameProfile profile = NBTHelper.profileFromNBT(modData);
			cache.put(profile, ticket);
			int[] x = modData.getIntArray("x");
			int[] z = modData.getIntArray("z");

			if (x.length == z.length) {
				for (int i = 0; i < x.length; i++) {
					ForgeChunkManager.forceChunk(ticket, new ChunkPos(x[i], z[i]));
					Chunk chunk = world.getChunkFromChunkCoords(x[i], z[i]);
				}
			}

		}
	}


	public void getDebug(List<String> info) {
		DescribeHelper.addDescription(info, "Chunk Loaders", chunkLoaders);
		DescribeHelper.addDescription(info, "Player Tickets", playerTickets);
	}

	@Override
	public ListMultimap<String, ForgeChunkManager.Ticket> playerTicketsLoaded(ListMultimap<String, ForgeChunkManager.Ticket> tickets, World world) {
		return tickets;
	}
}
