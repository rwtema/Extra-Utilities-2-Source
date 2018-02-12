package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.utils.datastructures.InitMap;
import com.rwtema.extrautils2.utils.datastructures.WeakSet;
import java.util.WeakHashMap;
import net.minecraft.world.World;

public class LocationRegistry {
	public static LocationRegistry sunTorches = new LocationRegistry();
	public static LocationRegistry magnumTorches = new LocationRegistry();

	InitMap<World, WeakSet<IRange>> locations = new InitMap<World, WeakSet<IRange>>(new WeakHashMap()) {
		@Override
		protected WeakSet<IRange> initValue(World key) {
			return new WeakSet<>();
		}
	};


	public void register(IRange range) {
		World world = range.world();
		if (world.isRemote) return;
		WeakSet<IRange> iRanges = locations.get(world);
		iRanges.add(range);
	}

	public void unregister(IRange range) {
		World world = range.world();
		WeakSet<IRange> iRanges = locations.get(world);
		iRanges.remove(range);
		if (iRanges.isEmpty()) locations.remove(world);
	}

	public boolean inRange(World world, double x, double y, double z) {
		if (locations.isEmpty() || !locations.containsKey(world)) return false;
		WeakSet<IRange> iRanges = locations.get(world);
		if (iRanges.isEmpty()) {
			locations.remove(world);
			return false;
		}

		for (IRange iRange : iRanges) {
			if (iRange.inRange(x, y, z)) return true;
		}
		return false;
	}
}
