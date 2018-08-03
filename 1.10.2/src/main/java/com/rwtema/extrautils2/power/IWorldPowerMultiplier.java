package com.rwtema.extrautils2.power;

import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;

public interface IWorldPowerMultiplier {
	IWorldPowerMultiplier CONSTANT = new IWorldPowerMultiplier() {
		@Override
		public float multiplier(World world) {
			return 1;
		}

		@Override
		public String toString() {
			return "CONSTANT";
		}
	};

	static float capPower(float input, @Nullable TreeMap<Float, Pair<Float, Float>> efficiencyLevels) {
		if (efficiencyLevels == null || efficiencyLevels.isEmpty() || input < efficiencyLevels.firstKey()) return input;

		Map.Entry<Float, Pair<Float, Float>> calc = efficiencyLevels.floorEntry(input);
		if (calc == null) return input;
		return calc.getValue().getKey() + (input - calc.getKey()) * calc.getValue().getValue();
	}

	static TreeMap<Float, Pair<Float, Float>> createCapsTree(float[][] caps) {
		if (caps.length == 0) return null;
		TreeMap<Float, Pair<Float, Float>> map = new TreeMap<>();
		float curTotal = caps[0][0];

		if (curTotal != 0) {
			map.put(0F, Pair.of(0F, 1F));
		}
		for (int i = 0; i < caps.length; i++) {
			float prevLower = caps[i][0];
			float multiplier = caps[i][1];
			map.put(prevLower, Pair.of(curTotal, multiplier));
			if (i + 1 < caps.length) {
				float nextLevel = caps[i + 1][0];
				curTotal += (nextLevel - prevLower) * multiplier;
			}
		}
		return map;
	}

	float multiplier(@Nullable World world);

	default IWorldPowerMultiplier getStaticVariation() {
		return CONSTANT;
	}

	default float alterTotal(float value) {
		return value;
	}

	default boolean hasInefficiencies() {
		return false;
	}
}
