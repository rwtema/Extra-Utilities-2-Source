package com.rwtema.extrautils2.utils.datastructures;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class PosFacing {

	@Nonnull
	final BlockPos pos;
	@Nonnull
	final EnumFacing side;

	public PosFacing(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		this.pos = pos;
		this.side = side;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PosFacing)) return false;

		PosFacing posFacing = (PosFacing) o;

		return pos.equals(posFacing.pos) && side == posFacing.side;
	}

	@Override
	public int hashCode() {
		int result = pos.hashCode();
		result = 31 * result + side.hashCode();
		return result;
	}

	@Nonnull
	public BlockPos getPos() {
		return pos;
	}

	@Nonnull
	public EnumFacing getSide() {
		return side;
	}
}
