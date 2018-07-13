package com.rwtema.extrautils2.tile;

import net.minecraft.world.World;

import java.util.LinkedHashSet;
import java.util.WeakHashMap;

public class TileLaserBeam extends TilePower {

	@Override
	public float getPower() {
		return 0;
	}

	@Override
	public void onPowerChanged() {

	}


	public static class LaserFenceData {
		WeakHashMap<World, LaserFenceData> map = new WeakHashMap<>();

		public static class LaserWorldData {
			LinkedHashSet<TileLaserBeam> fenceList = new LinkedHashSet<>();


			public void register(TileLaserBeam fence) {
				if (fenceList.add(fence)) {

				}
			}
		}
	}
}
