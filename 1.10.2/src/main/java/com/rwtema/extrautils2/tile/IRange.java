package com.rwtema.extrautils2.tile;

import net.minecraft.world.World;

public interface IRange {
	boolean inRange(double x, double y, double z);

	World world();
}
