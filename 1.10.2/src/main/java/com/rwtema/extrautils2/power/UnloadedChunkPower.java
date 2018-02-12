package com.rwtema.extrautils2.power;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.rwtema.extrautils2.blocks.BlockPassiveGenerator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UnloadedChunkPower implements INBTSerializable<NBTTagList> {
	static final BiMap<String, IWorldPowerMultiplier> passiveRegistry = HashBiMap.create();
	static final BiMap<IWorldPowerMultiplier, String> passiveRegistryInv = passiveRegistry.inverse();

	static {
		passiveRegistry.put("Constant", IWorldPowerMultiplier.CONSTANT);
		passiveRegistry.put("Solar", BlockPassiveGenerator.GeneratorType.SOLAR);
		passiveRegistry.put("Lunar", BlockPassiveGenerator.GeneratorType.LUNAR);
		passiveRegistry.put("Wind", BlockPassiveGenerator.GeneratorType.WIND);
		passiveRegistry.put("Water", BlockPassiveGenerator.GeneratorType.WATER);
		passiveRegistry.put("Lava", BlockPassiveGenerator.GeneratorType.LAVA);
		passiveRegistry.put("Fire", BlockPassiveGenerator.GeneratorType.FIRE);
		passiveRegistry.put("Dragon", BlockPassiveGenerator.GeneratorType.DRAGON_EGG);
	}

	public TIntObjectHashMap<FreqData> freqs = new TIntObjectHashMap<>();
	boolean dirty = true;

	public void rebuild() {

		freqs.retainEntries((a, b) -> {
			b.clearLoadedChunks();
			return !b.isEmpty();
		});

		PowerManager instance = PowerManager.instance;

		for (IPower powerHandler : instance.assignedValues.keySet()) {
			if (!powerHandler.isLoaded()) continue;
			World world = powerHandler.world();
			BlockPos location = powerHandler.getLocation();
			if (world == null || location == null) continue;
			IWorldPowerMultiplier multiplier = powerHandler.getMultiplier();
			if (!passiveRegistryInv.containsKey(multiplier)) continue;
			float power = powerHandler.getPower();
			if (power == 0 || Float.isNaN(power)) continue;

			int frequency = powerHandler.frequency();

			FreqData freqData = freqs.get(frequency);
			if (freqData == null) {
				freqData = new FreqData();
				freqs.put(frequency, freqData);
			}

			freqData.addPowerEntry(world, location, multiplier, power);
		}

		PowerSettings.instance.markDirty();
	}

	@Override
	public NBTTagList serializeNBT() {
		NBTTagList freqList = new NBTTagList();
		freqs.forEachEntry((a, b) -> {
			NBTTagCompound freqData = new NBTTagCompound();
			freqData.setInteger("Freq", a);
			freqData.setTag("Data", b.serializeNBT());
			freqList.appendTag(freqData);
			return true;
		});

		return freqList;
	}

	@Override
	public void deserializeNBT(NBTTagList nbt) {
		freqs.clear();
		for (int i = 0; i < nbt.tagCount(); i++) {
			NBTTagCompound freqData = nbt.getCompoundTagAt(i);
			int freq = freqData.getInteger("Freq");
			FreqData data = new FreqData();
			data.deserializeNBT(freqData.getCompoundTag("Data"));
			freqs.put(freq, data);
		}
	}


	public static class FreqData implements INBTSerializable<NBTTagCompound> {
		TIntObjectHashMap<HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> worldChunkDataCreators = new TIntObjectHashMap<>();
		TIntObjectHashMap<HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> worldChunkDataDrainers = new TIntObjectHashMap<>();

		private TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> unloadedPowerCreators = new TIntObjectHashMap<>();
		private TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> unloadedPowerDrainers = new TIntObjectHashMap<>();

		public float getPowerCreated() {
			return getPower(unloadedPowerCreators);
		}

		public float getPowerDrained() {
			return getPower(unloadedPowerDrainers);
		}

		public void getPowerCreated(TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeCreators) {
			getPower(unloadedPowerCreators, rawTypeCreators);
		}

		public void getPowerDrained(TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeDrainers) {
			getPower(unloadedPowerDrainers, rawTypeDrainers);
		}

		public float getPower(TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> unloadedPowerMap) {
			float t = 0;
			for (int dim : unloadedPowerMap.keys()) {
				TObjectFloatHashMap<IWorldPowerMultiplier> map = unloadedPowerMap.get(dim);
				WorldServer world = DimensionManager.getWorld(dim);
				if (world == null) {
					if (map.containsKey(IWorldPowerMultiplier.CONSTANT)) {
						t += map.get(IWorldPowerMultiplier.CONSTANT);
					}
				} else {
					for (IWorldPowerMultiplier multiplier : map.keySet()) {
						t += multiplier.multiplier(world) * map.get(multiplier);
					}
				}
			}
			return t;
		}

		public void getPower(TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> unloadedPowerMap, TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> rawTypeMap) {

			for (int dim : unloadedPowerMap.keys()) {
				TObjectFloatHashMap<IWorldPowerMultiplier> map = unloadedPowerMap.get(dim);

				TObjectFloatHashMap<IWorldPowerMultiplier> worldMap = rawTypeMap.get(dim);
				if (worldMap == null) {
					rawTypeMap.put(dim, worldMap = new TObjectFloatHashMap<>());
				}

				for (IWorldPowerMultiplier worldPowerMultiplier : map.keySet()) {
					float v = map.get(worldPowerMultiplier);
					worldMap.adjustOrPutValue(worldPowerMultiplier, v, v);
				}
			}
		}

		public void clearLoadedChunks() {
			unload(worldChunkDataCreators, unloadedPowerCreators);
			unload(worldChunkDataDrainers, unloadedPowerDrainers);
		}

		public void unload(TIntObjectHashMap<HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> worldChunkData, TIntObjectHashMap<TObjectFloatHashMap<IWorldPowerMultiplier>> unloadedPower) {
			unloadedPower.clear();
			worldChunkData.retainEntries((dimension, chunkMap) -> {
				WorldServer world = DimensionManager.getWorld(dimension);
				if (world == null) return true;
				ChunkProviderServer chunkProvider = world.getChunkProvider();
				for (Iterator<Map.Entry<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> iterator = chunkMap.entrySet().iterator(); iterator.hasNext(); ) {
					Map.Entry<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>> entry = iterator.next();
					ChunkPos key = entry.getKey();
					TObjectFloatHashMap<IWorldPowerMultiplier> powerMap = entry.getValue();

					long l = ChunkPos.asLong(key.x, key.z);
					if (chunkProvider.id2ChunkMap.containsKey(l)) {
						iterator.remove();
						continue;
					}

					powerMap.forEachEntry((a, b) -> {
						TObjectFloatHashMap<IWorldPowerMultiplier> worldMap = unloadedPower.get(dimension);
						if (worldMap == null) {
							worldMap = new TObjectFloatHashMap<>();
							unloadedPower.put(dimension, worldMap);
						}

						worldMap.adjustOrPutValue(a, b, b);
						return true;
					});
				}
				return !chunkMap.isEmpty();
			});
		}

		public boolean isEmpty() {
			return worldChunkDataCreators.isEmpty() && worldChunkDataDrainers.isEmpty();
		}

		public void addPowerEntry(World world, BlockPos location, IWorldPowerMultiplier multiplier, float power) {
			int dim = world.provider.getDimension();
			ChunkPos pos = new ChunkPos(location);
			TIntObjectHashMap<HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> worldChunkData;
			if (power == 0 || Float.isNaN(power)) {
				return;
			} else if (power < 0) {
				power *= -1;
				worldChunkData = this.worldChunkDataCreators;
			} else {
				worldChunkData = this.worldChunkDataDrainers;
			}

			HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>> chunkMap = worldChunkData.get(dim);
			if (chunkMap == null) {
				worldChunkData.put(dim, chunkMap = new HashMap<>());
			}

			TObjectFloatHashMap<IWorldPowerMultiplier> powers = chunkMap.computeIfAbsent(pos, chunkPos -> new TObjectFloatHashMap<>());

			powers.adjustOrPutValue(multiplier, power, power);
		}


		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound data = new NBTTagCompound();
			data.setTag("Creators", serializeList(worldChunkDataCreators));
			data.setTag("Drainers", serializeList(worldChunkDataDrainers));
			return data;
		}

		@Nonnull
		public NBTTagList serializeList(TIntObjectHashMap<HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> worldChunkDataCreators) {
			NBTTagList worldList = new NBTTagList();
			worldChunkDataCreators.forEachEntry((dimension, chunkMap) -> {
				NBTTagCompound worldData = new NBTTagCompound();
				NBTTagList chunkList = new NBTTagList();
				chunkMap.forEach((chunkCoord, powerMap) -> {
					NBTTagCompound chunkData = new NBTTagCompound();
					chunkData.setInteger("CoordX", chunkCoord.x);
					chunkData.setInteger("CoordZ", chunkCoord.z);
					for (IWorldPowerMultiplier multiplier : powerMap.keySet()) {
						String s = passiveRegistryInv.get(multiplier);
						chunkData.setFloat(s, powerMap.get(multiplier));
					}
					chunkList.appendTag(chunkData);
				});
				worldData.setTag("Chunks", chunkList);
				worldData.setInteger("Dim", dimension);
				worldList.appendTag(worldData);
				return true;
			});
			return worldList;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			deserialize(nbt, "Creators", worldChunkDataCreators);
			deserialize(nbt, "Drainers", worldChunkDataDrainers);
		}

		public void deserialize(NBTTagCompound nbt, String creators, TIntObjectHashMap<HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>>> worldChunkData) {
			worldChunkData.clear();
			NBTTagList worldList = nbt.getTagList(creators, Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < worldList.tagCount(); i++) {
				NBTTagCompound worldData = worldList.getCompoundTagAt(i);
				int dim = worldData.getInteger("Dim");
				NBTTagList chunkList = worldData.getTagList("Chunks", Constants.NBT.TAG_COMPOUND);
				HashMap<ChunkPos, TObjectFloatHashMap<IWorldPowerMultiplier>> chunkMap = new HashMap<>();

				for (int j = 0; j < chunkList.tagCount(); j++) {
					NBTTagCompound chunkData = chunkList.getCompoundTagAt(j);

					TObjectFloatHashMap<IWorldPowerMultiplier> multipliers = new TObjectFloatHashMap<>();
					for (Map.Entry<String, IWorldPowerMultiplier> entry : passiveRegistry.entrySet()) {
						if (chunkData.hasKey(entry.getKey())) {
							multipliers.put(entry.getValue(), chunkData.getFloat(entry.getKey()));
						}
					}
					ChunkPos coord = new ChunkPos(chunkData.getInteger("CoordX"), chunkData.getInteger("CoordZ"));
					chunkMap.put(coord, multipliers);
				}

				worldChunkData.put(dim, chunkMap);
			}
		}
	}


}
