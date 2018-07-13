package com.rwtema.extrautils2.utils.datastructures;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;

public abstract class LazySideMask {
	byte calc;
	byte result;

	public boolean get(TileEntity parent, EnumFacing side) {
		return get(side.ordinal(), parent);
	}

	public boolean isEmpty(TileEntity parent) {
		if (calc != (1 | 2 | 4 | 8 | 16 | 32)) {
			for (int i = 0; i < 6; i++) {
				get(i, parent);
			}
		}
		return result == 0;
	}

	protected boolean get(int bitIndex, TileEntity parent) {
		int mask = 1 << bitIndex;
		if ((calc & mask) == 0) {
			calc |= mask;
			if (test(bitIndex, parent)) {
				result |= mask;
			} else {
				result &= ~mask;
			}
		}
		return (result & mask) != 0;
	}

	protected boolean test(int bitIndex, TileEntity tile) {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		EnumFacing facing = EnumFacing.values()[bitIndex];
		BlockPos offset = pos.offset(facing);
		TileEntity tileEntity = world.getTileEntity(offset);
		return test(tileEntity, world, offset, facing.getOpposite(), tile, bitIndex);
	}

	protected abstract boolean test(@Nullable TileEntity tileEntity, World world, BlockPos offset, EnumFacing opposite, TileEntity tile, int bitIndex);

	public void invalidateAll() {
		calc = 0;
	}

	public void invalidate(int bitIndex) {
		calc &= (1 << bitIndex);
	}

	public static abstract class HasCap extends LazySideMask {

		@Override
		protected boolean test(TileEntity tileEntity, World world, BlockPos offset, EnumFacing opposite, TileEntity tile, int bitIndex) {
			return tileEntity != null && tileEntity.hasCapability(getCapability(), opposite);
		}

		protected abstract Capability<?> getCapability();

		public static class Energy extends HasCap {
			@Override
			protected Capability<?> getCapability() {
				return CapabilityEnergy.ENERGY;
			}
		}
	}
}
