package com.rwtema.extrautils2.utils.blockaccess;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockAccessSingle extends BlockAccessEmpty {
	public static final BlockPos CENTER = new BlockPos(0, 128, 0);
	public static final LoadingCache<IBlockState, BlockAccessSingle> cache = CacheBuilder.newBuilder().maximumSize(60).build(new CacheLoader<IBlockState, BlockAccessSingle>() {
		@Override
		public BlockAccessSingle load(@Nullable IBlockState key) throws Exception {
			return new BlockAccessSingle(key);
		}
	});
	IBlockState state;

	public BlockAccessSingle(IBlockState state) {

		this.state = state;
	}

	@Nonnull
	@Override
	public IBlockState getBlockState(@Nonnull BlockPos pos) {
		if (pos.equals(CENTER))
			return state;
		return super.getBlockState(pos);
	}
}
