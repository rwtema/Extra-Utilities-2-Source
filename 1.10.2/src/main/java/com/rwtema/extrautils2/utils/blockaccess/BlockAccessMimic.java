package com.rwtema.extrautils2.utils.blockaccess;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockAccessMimic extends BlockAccessDelegate {

	public IBlockState state;
	public BlockPos myPos;

	public BlockAccessMimic() {
		super();
	}

	public BlockAccessMimic(@Nullable IBlockAccess base, BlockPos myPos, IBlockState state) {
		super(base);
		this.state = state;
		this.myPos = myPos;
	}

	@Nonnull
	@Override
	public IBlockState getBlockState(@Nonnull BlockPos pos) {
		if (state != null && pos.equals(myPos)) return state;
		return super.getBlockState(pos);
	}

	@Override
	public boolean isAirBlock(@Nonnull BlockPos pos) {
		if (state != null && pos.equals(myPos)) return state.getBlock() == Blocks.AIR;
		return super.isAirBlock(pos);
	}


}
