package com.rwtema.extrautils2.power;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.power.player.PlayerPowerManager;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.util.*;

import static com.rwtema.extrautils2.utils.helpers.DescribeHelper.addDescription;


public class PowerManager {
	public final static PowerManager instance = new PowerManager();
	public static final Object MUTEX = new Object();
	private static final int REFRESH_TIME = 20 * 30;
	public final TIntHashSet lockedFrequencies = new TIntHashSet();
	public final TIntObjectHashMap<GameProfile> frequncies = new TIntObjectHashMap<>();
	public final WeakHashMap<EntityPlayerMP, PowerFreq> assignedValuesPlayer = new WeakHashMap<>();
	public final TIntObjectHashMap<TIntHashSet> alliances = new TIntObjectHashMap<>();
	public final ReferenceQueue<Object> weakPowersToRemove = new ReferenceQueue<>();
	public final TIntObjectHashMap<PowerFreq> frequencyHolders = new TIntObjectHashMap<>();
	private final TIntIntHashMap links = new TIntIntHashMap();
	private final WeakLinkedSet<IPower> powersToAdd = new WeakLinkedSet<>();
	private final WeakLinkedSet<IPower> powersToRemove = new WeakLinkedSet<>();
	public WeakHashMap<IPower, PowerFreq> assignedValues = new WeakHashMap<>();
	int p = 0;
	boolean dirty = true;
	UnloadedChunkPower unloadedChunkManager = new UnloadedChunkPower();
	private boolean playersDirty = true;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(instance);
		MinecraftForge.EVENT_BUS.register(new PlayerPowerManager());
	}


	public static float getEfficiency(IPower power) {
		IWorldPowerMultiplier multiplier = power.getMultiplier();
		if (!multiplier.hasInefficiencies()) return 1;

		PowerFreq freq = PowerManager.instance.getPowerFreq(power.frequency());
		float v = power.getPower();
		float total = 0;
		WeakHashMap<World, TObjectFloatHashMap<IWorldPowerMultiplier>> providers = v < 0 ? freq.worldPowerCreators : freq.worldPowerDrainers;
		for (Map.Entry<World, TObjectFloatHashMap<IWorldPowerMultiplier>> entry : providers.entrySet()) {
			if (entry.getValue().containsKey(multiplier)) {
				float am = entry.getValue().get(multiplier);
				if (am != Float.NaN) {
					total += am * multiplier.multiplier(entry.getKey());
				}
			}
		}
		float v1 = multiplier.alterTotal(total);

		return v1 / total;
	}

	public static float getCurrentPower(IPower power) {

		float v = power.getPower();
		if (v == 0) return 0;
		IWorldPowerMultiplier multiplier = power.getMultiplier();

		return v * multiplier.multiplier(power.world());
	}

	public static boolean areFreqOnSameGrid(int freq, int frequency) {
		return freq == frequency || instance.getPowerFreq(freq) == instance.getPowerFreq(frequency);
	}

	public static boolean canUse(EntityPlayer player, IPower power) {
		if (!(player instanceof EntityPlayerMP)) return false;
		if (!PowerManager.instance.lockedFrequencies.contains(power.frequency())) {
			return true;
		}
		int basePlayerFreq = Freq.getBasePlayerFreq((EntityPlayerMP) player);
		return basePlayerFreq != 0 && areFreqOnSameGrid(power.frequency(), basePlayerFreq);
	}

	public static void addPulse(int frequency, float pulseTime) {
		if (pulseTime == 0) return;
		long t = (long) Math.ceil(pulseTime / FrequencyPulses.COST_PER_TICK);
		addPulseTime(frequency, t);
	}

	public static void addPulseTime(int frequency, long t) {
		if (t == 0) return;
		long totalWorldTime = DimensionManager.getWorld(0).getTotalWorldTime();
		long offset;
		if (FrequencyPulses.pulsesMap.containsKey(frequency)) {
			long prevPulseTimer = FrequencyPulses.pulsesMap.get(frequency);
			offset = prevPulseTimer - totalWorldTime;
			if (offset < 0) {
				offset = 0;
			}
		} else {
			offset = 0;
		}

		FrequencyPulses.pulsesMap.put(frequency, totalWorldTime + t + offset);
		FrequencyPulses.INSTANCE.markDirty();
	}

	public boolean sameTeam(int a, int b) {
		if (a == b) return true;

		if (links.containsKey(a)) a = links.get(a);
		if (links.containsKey(b)) b = links.get(b);

		return a == b;
	}

	public void clear() {
		synchronized (MUTEX) {
			frequncies.clear();

			p = 0;
			//noinspection StatementWithEmptyBody
			while (weakPowersToRemove.poll() != null) ;
			links.clear();
			frequencyHolders.clear();
			powersToAdd.clear();
			powersToRemove.clear();
			assignedValuesPlayer.clear();
			playersDirty = true;
			assignedValues.clear();
			alliances.clear();
			unloadedChunkManager.freqs.clear();
		}
	}

	public void getDebug(final List<String> info) {
		TIntObjectProcedure<Object> procedure = (a, b) -> {
			info.add(a + "=" + b.toString());
			return true;
		};

		addDescription(info, "Frequencies");
		frequncies.forEachEntry(procedure);
		addDescription(info, "Alliances");
		alliances.forEachEntry(procedure);
		addDescription(info, "AssignedValues");
		addDescription(info, assignedValues);
		addDescription(info, "Frequencie Holders");
		frequencyHolders.forEachEntry(new TIntObjectProcedure<PowerFreq>() {
			@Override
			public boolean execute(int a, PowerFreq b) {
				addDescription(info, "Init");
				describe(a, b);
				addDescription(info, "QRefresh");
				b.quickRefresh();
				describe(a, b);
				addDescription(info, "FRefresh");
				b.refresh();
				describe(a, b);
				return true;
			}

			public void describe(int a, PowerFreq b) {
				addDescription(info, "Freq = " + a, 2);
				addDescription(info, "Power", 2);
				addDescription(info, b.powerDrained + " " + b.powerCreated, 3);
				addDescription(info, "Players", 2);
				addDescription(info, b.players, 3);
				addDescription(info, "Creators", 2);
				addDescription(info, b.worldPowerCreators, 3);
				addDescription(info, "Drainers", 2);
				addDescription(info, b.worldPowerDrainers, 3);
				addDescription(info, "IPowers", 2);
				addDescription(info, b.powerHandlers, 3);
			}
		});
	}

	public void reassignValues() {
		dirty = true;
		playersDirty = true;
	}

	public void remake() {
		synchronized (MUTEX) {
			dirty = false;
			playersDirty = true;
			unloadedChunkManager.dirty = true;

			PowerSettings.instance.markDirty();
			links.clear();
			for (EntityPlayerMP entityPlayerMP : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				frequncies.put(Freq.getBasePlayerFreq(entityPlayerMP), entityPlayerMP.getGameProfile());
			}

			int[] keys = frequncies.keys();

			TIntObjectHashMap<TIntHashSet> mutualAlliances = new TIntObjectHashMap<>();
			for (int a : keys) {
				TIntHashSet set = new TIntHashSet();

				for (int b : keys) {
					if (areAAndBMutualAllies(a, b))
						set.add(b);
				}

				mutualAlliances.put(a, set);
			}


			for (int a : keys) {
				if (links.containsKey(a)) continue;

				TIntLinkedList toProcess = new TIntLinkedList();
				toProcess.add(a);

				while (!toProcess.isEmpty()) {
					int b = toProcess.removeAt(0);
					if (links.containsKey(b)) continue;
					links.put(b, a);
					TIntHashSet allies = mutualAlliances.get(b);
					if (allies != null) {
						toProcess.addAll(allies);
					}
				}
			}

			for (int key : keys) {
				if (links.get(key) == key)
					links.remove(key);
			}

			for (PowerFreq powerFreq : frequencyHolders.valueCollection()) {
				powersToAdd.addAll(powerFreq.powerHandlers);
			}

			frequencyHolders.clear();
		}
	}

	public void addPowerHandler(IPower needer) {
		synchronized (MUTEX) {
			powersToAdd.add(needer);
		}
	}

	public boolean isPowered(EntityPlayerMP player) {
		PowerFreq powerFreq = assignedValuesPlayer.get(player);
		if (powerFreq == null) {
			if (!PlayerHelper.isPlayerReal(player)) return false;
			powerFreq = getPowerFreq(Freq.getBasePlayerFreq(player));
		}
		return powerFreq != null && powerFreq.isPowered();
	}

	@Nullable
	public PowerFreq getPowerFreqRaw(int frequency) {
		int i = links.containsKey(frequency) ? links.get(frequency) : frequency;
		return frequencyHolders.get(i);
	}

	public PowerFreq getPowerFreq(int frequency) {
		int i = links.containsKey(frequency) ? links.get(frequency) : frequency;

		PowerFreq powerFreq = frequencyHolders.get(i);
		if (powerFreq == null) {
			powerFreq = new PowerFreq(i);
			powerFreq.dirty = true;
			frequencyHolders.put(i, powerFreq);
		}
		return powerFreq;
	}

	public boolean areAAndBMutualAllies(int a, int b) {
		return doesAWishToAllyWithB(a, b) && doesAWishToAllyWithB(b, a);
	}

	public boolean doesAWishToAllyWithB(int a, int b) {
		TIntHashSet tIntHashSet = alliances.get(a);
		return tIntHashSet != null && tIntHashSet.contains(b);
	}

	public void removePowerHandler(IPower needer) {
		synchronized (MUTEX) {
			powersToRemove.add(needer);
		}
	}

	public void markDirty(IPower power) {
		PowerFreq powerFreq = assignedValues.get(power);
		if (powerFreq == null) powerFreq = getPowerFreq(power.frequency());
		powerFreq.dirty = true;
	}

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Load event) {
		if (!event.getWorld().isRemote)
			unloadedChunkManager.dirty = true;
	}

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		unloadedChunkManager.dirty = true;
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerChangedDimensionEvent event) {
		playersDirty = true;
	}

	@SubscribeEvent
	public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event){
		playersDirty = true;
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerRespawnEvent event) {
		playersDirty = true;
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		playersDirty = true;
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedOutEvent event) {
		playersDirty = true;
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		p++;
		if (p > REFRESH_TIME) p = 0;

		synchronized (MUTEX) {
			if (dirty)
				remake();

			for (Object x; (x = weakPowersToRemove.poll()) != null; ) {
				powersToRemove.add((IPower) x);
				unloadedChunkManager.dirty = true;
			}


			if (!powersToRemove.isEmpty()) {

				for (IPower iPower : powersToRemove) {
					PowerFreq powerFreq = assignedValues.get(iPower);
					if (powerFreq == null) powerFreq = getPowerFreq(iPower.frequency());
					powerFreq.powerHandlers.remove(iPower);
					powerFreq.subTypes = null;
					powerFreq.dirty = true;
				}
				powersToRemove.clear();

				unloadedChunkManager.dirty = true;
			}

			if (!powersToAdd.isEmpty()) {

				for (IPower iPower : powersToAdd) {
					if (!iPower.isLoaded()) continue;

					PowerFreq powerFreq = getPowerFreq(iPower.frequency());
					powerFreq.powerHandlers.add(iPower);
					powerFreq.subTypes = null;
					if (assignedValues.containsKey(iPower)) {
						PowerFreq oldFreq = assignedValues.get(iPower);
						oldFreq.powerHandlers.remove(iPower);
						oldFreq.subTypes = null;
						oldFreq.dirty = true;
					}
					assignedValues.put(iPower, powerFreq);
					powerFreq.dirty = true;
				}
				powersToAdd.clear();

				unloadedChunkManager.dirty = true;
			}

			if (playersDirty) {
				assignedValuesPlayer.clear();

				TIntObjectIterator<PowerFreq> iterator = frequencyHolders.iterator();
				while (iterator.hasNext()) {
					iterator.advance();

					PowerFreq value = iterator.value();
					value.playerFreqs.clear();
					value.players.clear();
				}

				LinkedHashSet<PowerFreq> freqs = new LinkedHashSet<>();
				for (EntityPlayerMP entityPlayerMP : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
					int basePlayerFreq = Freq.getBasePlayerFreq(entityPlayerMP);
					PowerFreq freq = getPowerFreq(basePlayerFreq);
					assignedValuesPlayer.put(entityPlayerMP, freq);
					freq.players.add(entityPlayerMP);
					freq.playerFreqs.add(basePlayerFreq);
					freqs.add(freq);
				}

				for (PowerFreq freq : freqs) {
					freq.dirty = true;
				}

				unloadedChunkManager.dirty = true;

				playersDirty = false;
			}

			if (unloadedChunkManager.dirty) {
				unloadedChunkManager.dirty = false;
				unloadedChunkManager.rebuild();
			}


			TIntObjectIterator<PowerFreq> iterator = frequencyHolders.iterator();
			while (iterator.hasNext()) {
				iterator.advance();
				PowerFreq value = iterator.value();
				if (value.dirty || value.refresh_delta == p) {
					if (value.refresh()) {
						value.dirty = false;
					} else {
						iterator.remove();
					}
				} else
					value.quickRefresh();
			}
		}
	}

	public interface IPowerReport {
		boolean isPowered();

		float getPowerDrain();

		float getPowerCreated();
	}

	public static class PowerFreq implements IPowerReport {
		final int frequency;
		public WeakLinkedSet<IPower> powerHandlers = new WeakLinkedSet<>();

		float powerDrained;
		float powerCreated;
		TIntHashSet playerFreqs = new TIntHashSet();
		WeakLinkedSet<EntityPlayerMP> players = new WeakLinkedSet<>();
		int refresh_delta = XURandom.rand.nextInt(REFRESH_TIME);
		boolean dirty = true;
		private WeakHashMap<World, TObjectFloatHashMap<IWorldPowerMultiplier>> worldPowerCreators = new WeakHashMap<>();
		private WeakHashMap<World, TObjectFloatHashMap<IWorldPowerMultiplier>> worldPowerDrainers = new WeakHashMap<>();

		private HashMap<ResourceLocation, Collection<IPowerSubType>> subTypes;

		public PowerFreq(int frequency) {
			this.frequency = frequency;
		}

		@Nullable
		public <T extends IPower> Collection<T> getSubTypes(ResourceLocation location) {
			HashMap<ResourceLocation, Collection<IPowerSubType>> map = this.subTypes;
			if (map == null) {
				synchronized (MUTEX) {
					map = this.subTypes;
					if (map == null) {
						this.subTypes = map = new HashMap<>();
						for (IPower power : powerHandlers) {
							if (power instanceof IPowerSubType) {
								for (ResourceLocation resourceLocation : ((IPowerSubType) power).getTypes()) {
									map.computeIfAbsent(resourceLocation, f -> new WeakLinkedSet<>())
											.add((IPowerSubType) power);
								}

							}
						}
					}
				}
			}
			return (Collection<T>) map.get(location);
		}

		public boolean refresh() {
			worldPowerDrainers.clear();
			worldPowerCreators.clear();
			for (IPower powerHandler : powerHandlers) {
				if (!powerHandler.isLoaded()) continue;

				float i = powerHandler.getPower();
				if (Float.isNaN(i)) continue;

				IWorldPowerMultiplier multiplier = powerHandler.getMultiplier();
				WeakHashMap<World, TObjectFloatHashMap<IWorldPowerMultiplier>> type;
				if (i > 0) {
					type = worldPowerDrainers;
				} else {
					type = worldPowerCreators;
					i = -i;
				}

				World world = powerHandler.world();
				TObjectFloatHashMap<IWorldPowerMultiplier> typeMap = type.computeIfAbsent(world, k -> new TObjectFloatHashMap<>());

				typeMap.adjustOrPutValue(multiplier, i, i);
			}


			quickRefresh();

			return !powerHandlers.isEmpty() || !players.isEmpty();
		}


		public void quickRefresh() {
			boolean oldPowered = powerDrained <= powerCreated;

			float prevCreated = powerCreated;
			float prevDrained = powerDrained;

			powerCreated = calcPower(worldPowerCreators);
			powerDrained = calcPower(worldPowerDrainers);

//			TIntIterator iterator = playerFreqs.iterator();
//			while (iterator.hasNext()) {
//				UnloadedChunkPower.FreqData freqData = instance.unloadedChunkManager.freqs.get(iterator.next());
//				if (freqData != null) {
//					powerCreated += freqData.getPowerCreated();
//					powerDrained += freqData.getPowerDrained();
//				}
//			}

//			TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeCreators = new TIntObjectHashMap<>();
//			TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeDrainers = new TIntObjectHashMap<>();
//
//			getRawTypes(rawTypeCreators, rawTypeDrainers);
//
//			powerCreated = 0;
//			powerDrained = 0;
//
//			TObjectFloatHashMap<IWorldPowerMultiplier> powerCreatorsTotal = new TObjectFloatHashMap<>();
//			TObjectFloatHashMap<IWorldPowerMultiplier> powerDrainersTotal = new TObjectFloatHashMap<>();
//
//			sumPowerDetails(powerCreatorsTotal, rawTypeCreators);
//			sumPowerDetails(powerDrainersTotal, rawTypeDrainers);
//
//			powerCreated = sum(powerCreatorsTotal);
//			powerDrained = sum(powerDrainersTotal);

			TIntIterator iterator = playerFreqs.iterator();
			long t = -1;
			while (iterator.hasNext()) {
				int freq = iterator.next();
				if (FrequencyPulses.pulsesMap.containsKey(freq)) {
					long l = FrequencyPulses.pulsesMap.get(freq);
					if (t == -1) {
						t = DimensionManager.getWorld(0).getTotalWorldTime();
					}

					long offset = l - t;
					if (offset < 0) {
						FrequencyPulses.pulsesMap.remove(freq);
						FrequencyPulses.INSTANCE.markDirty();
					} else {
						powerDrained += offset * FrequencyPulses.COST_PER_TICK;
					}
				}
			}

			boolean newPower = powerDrained <= powerCreated;

			if (dirty || newPower != oldPowered) {
				changeStatus(newPower);
			}

			if (dirty || prevCreated != powerCreated || prevDrained != powerDrained)
				sendNetworkUpdates();
		}

		public void getRawTypes(TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeCreators, TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeDrainers) {
			calcPower(worldPowerCreators, rawTypeCreators);
			calcPower(worldPowerDrainers, rawTypeDrainers);

			TIntIterator iterator = playerFreqs.iterator();
			while (iterator.hasNext()) {
				UnloadedChunkPower.FreqData freqData = instance.unloadedChunkManager.freqs.get(iterator.next());
				if (freqData != null) {
					freqData.getPowerCreated(rawTypeCreators);
					freqData.getPowerDrained(rawTypeDrainers);
				}
			}
		}

		private float calcPower(WeakHashMap<World, TObjectFloatHashMap<IWorldPowerMultiplier>> multiplierCache) {
			TObjectFloatHashMap<IWorldPowerMultiplier> sums = new TObjectFloatHashMap<>(10, 0.5F, 0);

			for (Map.Entry<World, TObjectFloatHashMap<IWorldPowerMultiplier>> entry : multiplierCache.entrySet()) {
				TObjectFloatIterator<IWorldPowerMultiplier> iterator = entry.getValue().iterator();
				while (iterator.hasNext()) {
					iterator.advance();
					IWorldPowerMultiplier powerMultiplier = iterator.key();
					float multiplier = powerMultiplier.multiplier(entry.getKey());
					if (Float.isNaN(multiplier)) continue;
					float value = iterator.value();
					float v = multiplier * value;
					sums.adjustOrPutValue(powerMultiplier, v, v);
				}
			}

			float p = 0;
			TObjectFloatIterator<IWorldPowerMultiplier> iterator = sums.iterator();
			while (iterator.hasNext()) {
				iterator.advance();
				p += iterator.key().alterTotal(iterator.value());
			}
			return p;
		}


		private void calcPower(WeakHashMap<World, TObjectFloatHashMap<IWorldPowerMultiplier>> multiplierCache, TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeMap) {
			for (Map.Entry<World, TObjectFloatHashMap<IWorldPowerMultiplier>> entry : multiplierCache.entrySet()) {
				TObjectFloatIterator<IWorldPowerMultiplier> iterator = entry.getValue().iterator();
				while (iterator.hasNext()) {
					iterator.advance();
					float v = iterator.value();
					if (!Float.isNaN(v)) {
						World key = entry.getKey();

						int dimension = key.provider.getDimension();
						TObjectFloatHashMap<IWorldPowerMultiplier> floatMap = rawTypeMap.get(dimension);
						if (floatMap == null) {
							rawTypeMap.put(dimension, floatMap = new TObjectFloatHashMap<>());
						}
						floatMap.adjustOrPutValue(iterator.key(), v, v);
					}

				}
			}

		}

//		private float calcPowerPlayer(WeakHashMap<EntityPlayerMP, TObjectFloatHashMap<IPowerItem>> multiplierCache) {
//			float p = 0;
//			for (Map.Entry<EntityPlayerMP, TObjectFloatHashMap<IPowerItem>> entry : multiplierCache.entrySet()) {
//				TObjectFloatIterator<IPowerItem> iterator = entry.getValue().iterator();
//				while (iterator.hasNext()) {
//					iterator.advance();
//					IPowerItem powerMultiplier = iterator.key();
//					float multiplier = powerMultiplier.getMultiplier(entry.getKey());
//					float value = iterator.value();
//					p += multiplier * value;
//				}
//			}
//
//			return p;
//		}

		public void sendNetworkUpdates() {
			for (EntityPlayerMP player : players) {
				NetworkHandler.sendPacketToPlayer(new PacketPower(powerCreated, powerDrained), player);
			}
		}

		public void changeStatus(boolean powered) {
			for (IPower powerNeeder : powerHandlers) {
				powerNeeder.powerChanged(powered);
			}

//			for (EntityPlayerMP player : players) {
//				HashSet<IPowerItem> powerItems = new HashSet<IPowerItem>();
//				TObjectFloatHashMap<IPowerItem> powerMap;
//				if ((powerMap = playerPowerDrainers.get(player)) != null) powerItems.addOverrides(powerMap.keySet());
//				if ((powerMap = playerPowerCreators.get(player)) != null) powerItems.addOverrides(powerMap.keySet());
//
//				for (IPowerItem powerItem : powerItems) {
//					powerItem.onChange(player, powered);
//				}
//			}
		}

		public boolean isPowered() {
			return powerDrained <= powerCreated;
		}

		@Override
		public float getPowerDrain() {
			return powerDrained;
		}

		@Override
		public float getPowerCreated() {
			return powerCreated;
		}
	}

	@NetworkHandler.XUPacket
	public static class PacketPower extends XUPacketServerToClient {
		float powerCreated;
		float powerUsed;

		public PacketPower() {

		}

		public PacketPower(float powerCreated, float powerUsed) {
			this.powerCreated = powerCreated;
			this.powerUsed = powerUsed;
		}

		@Override
		public void writeData() throws Exception {
			writeFloat(powerCreated);
			writeFloat(powerUsed);
		}

		@Override
		public void readData(EntityPlayer player) {
			powerCreated = readFloat();
			powerUsed = readFloat();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			ClientPower.powerCreated = powerCreated;
			ClientPower.powerDrained = powerUsed;
			return null;
		}
	}


}
