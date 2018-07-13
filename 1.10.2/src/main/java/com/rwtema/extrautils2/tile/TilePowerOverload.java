package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TilePowerOverload extends XUTile implements ITickable, IPower {
	@Override
	public float getPower() {
		return 1000000000f;
	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return IWorldPowerMultiplier.CONSTANT;
	}

	@Override
	public int frequency() {
		return 0;
	}

	@Override
	public void powerChanged(boolean powered) {

	}

	@Nullable
	@Override
	public World world() {
		return world;
	}

	@Nonnull
	@Override
	public String getName() {
		return getBlockState().getUnlocalizedName();
	}

	@Nullable
	@Override
	public BlockPos getLocation() {
		return getPos();
	}


	@Override
	public void update() {

	}
}
