package com.rwtema.extrautils2.power;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IPower {
	float getPower();

	IWorldPowerMultiplier getMultiplier();

	int frequency();

	void powerChanged(boolean powered);

	@Nullable
	World world();

	String getName();

	boolean isLoaded();

	@Nullable
	BlockPos getLocation();
}
