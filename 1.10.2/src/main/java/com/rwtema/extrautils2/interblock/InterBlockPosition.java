package com.rwtema.extrautils2.interblock;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class InterBlockPosition {
	@Nonnull
	final BlockPos lowerPos;
	@Nonnull
	final EnumFacing.Axis axis;

	public InterBlockPosition(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		axis = side.getAxis();
		if(side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE){
			lowerPos = pos;
		}else{
			lowerPos = pos.offset(side);
		}
	}

	public InterBlockPosition(@Nonnull BlockPos a, @Nonnull BlockPos b) {
		int dx = a.getX() - b.getX();
		int dy = a.getX() - b.getX();
		int dz = a.getX() - b.getX();

		if (dx != 0) {
			this.axis = EnumFacing.Axis.X;
			if (dy != 0 || dz != 0)
				throw new IllegalStateException();
			if (dx == -1) {
				this.lowerPos = a;
			} else if (dx == 1) {
				this.lowerPos = b;
			} else {
				throw new IllegalStateException();
			}
		} else if (dy != 0) {
			if (dz != 0) throw new IllegalStateException();
			this.axis = EnumFacing.Axis.X;
			if (dy == -1) {
				this.lowerPos = a;
			} else if (dy == 1) {
				this.lowerPos = b;
			} else {
				throw new IllegalStateException();
			}
		} else {
			this.axis = EnumFacing.Axis.Z;
			if (dz == -1) {
				this.lowerPos = a;
			} else if (dz == 1) {
				this.lowerPos = b;
			} else {
				throw new IllegalStateException();
			}
		}
	}

	public InterBlockPosition(@Nonnull BlockPos lowerPos, @Nonnull EnumFacing.Axis axis) {
		this.lowerPos = lowerPos;
		this.axis = axis;
	}

	@Nonnull
	public BlockPos getLowerPos() {
		return lowerPos;
	}

	@Nonnull
	public BlockPos getUpperPos() {
		switch (axis) {
			case X:
				return lowerPos.add(1, 0, 0);
			case Y:
				return lowerPos.add(0, 1, 0);
			case Z:
				return lowerPos.add(0, 0, 1);
		}
		throw new IllegalStateException();
	}
}
