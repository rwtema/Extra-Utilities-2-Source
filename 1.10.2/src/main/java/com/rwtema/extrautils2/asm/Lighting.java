package com.rwtema.extrautils2.asm;

import com.rwtema.extrautils2.lighting.ILight;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

public class Lighting {
	public static final WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> plusLights = new WeakHashMap<>();
	public static final WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> negLights = new WeakHashMap<>();
	private static final EnumSkyBlock[] removeTypes = new EnumSkyBlock[]{null, EnumSkyBlock.BLOCK, EnumSkyBlock.SKY};

	public static void register(ILight light, WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> lightType) {
		HashMap<EnumSkyBlock, Set<ILight>> enumMap = lightType.computeIfAbsent(light.getLightWorld(), k -> new HashMap<>());

		for (EnumSkyBlock type : light.getLightType()) {
			Set<ILight> lightMap = enumMap.get(type);
			if (lightMap == null) {
				lightMap = new WeakLinkedSet<>();
				enumMap.put(type, lightMap);
			}

			lightMap.add(light);
		}

		Set<ILight> lightMap = enumMap.get(null);
		if (lightMap == null) {
			lightMap = new WeakLinkedSet<>();
			enumMap.put(null, lightMap);
		}

		lightMap.add(light);
	}

	public static void unregister(ILight light, WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> lightType) {
		HashMap<EnumSkyBlock, Set<ILight>> map = lightType.get(light.getLightWorld());
		if (map == null) return;

		for (EnumSkyBlock type : removeTypes) {
			Set<ILight> lightMap = map.get(type);
			if (lightMap == null) continue;

			lightMap.remove(light);

			if (lightMap.isEmpty()) {
				map.remove(type);
				if (map.isEmpty()) {
					lightType.remove(light.getLightWorld());
				}
			}
		}
	}

	public static Set<ILight> getLightList(World world, BlockPos pos, EnumSkyBlock type, WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> lightType) {
		HashMap<EnumSkyBlock, Set<ILight>> enumMap = lightType.get(world);
		if (enumMap == null) return null;
		Set<ILight> worldMap = enumMap.get(type);
		if (worldMap == null) return null;
		if (worldMap.isEmpty()) {
			enumMap.remove(type);
			if (enumMap.isEmpty()) {
				lightType.remove(world);
			}
			return null;
		}
		return worldMap;
	}

	@SuppressWarnings("unused")
	public static int getLightFor(World world, EnumSkyBlock type, BlockPos pos, int curLevel) {
		if (plusLights.isEmpty() && negLights.isEmpty()) return curLevel;
		Set<ILight> negLightList = getLightList(world, pos, type, negLights);
		Set<ILight> plusLightList = getLightList(world, pos, type, plusLights);
		if (negLightList == null) {
			if (plusLightList == null) return curLevel;
			float level = curLevel;
			for (ILight iLight : plusLightList) {
				level += iLight.getLightOffset(pos, type);
				if (level > 15) return 15;
			}
			return Math.min((int) level, 15);
		} else {
			float level = curLevel;
			if (plusLightList != null) {
				for (ILight iLight : plusLightList) {
					level += iLight.getLightOffset(pos, type);
					if (level > 15) break;
				}
			}
			if (level > 15) level = 15;

			for (ILight iLight : negLightList) {
				level += iLight.getLightOffset(pos, type);
				if (level < 0) return 0;
			}
			return Math.max(0, (int) (level));
		}
	}

	@SuppressWarnings("unused")
	public static int getCombinedLight(World world, BlockPos pos, int curLevel) {
		if (plusLights.isEmpty() && negLights.isEmpty()) return curLevel;

		Set<ILight> plusLights = getLightList(world, pos, null, Lighting.plusLights);
		Set<ILight> negLights = getLightList(world, pos, null, Lighting.negLights);

		if (negLights == null) {
			if (curLevel == 15) return 15;

			float level = curLevel;
			if (plusLights != null)
				for (ILight iLight : plusLights) {
					level += iLight.getLightOffset(pos, null);
					if (level >= 15) return 15;
				}

			return (int) level;
		} else {
			float level = curLevel;
			if (plusLights != null)
				for (ILight iLight : plusLights) {
					level += iLight.getLightOffset(pos, null);
					if (level >= 15) break;
				}

			if (level > 15) level = 15;

			for (ILight iLight : negLights) {
				level += iLight.getLightOffset(pos, null);
				if (level <= 0) return 0;
			}

			return (int) level;
		}
	}
}
