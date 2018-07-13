package com.rwtema.extrautils2.utils.blockaccess;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class BlockAccessEmptyGlowing extends BlockAccessEmpty {
	public static final BlockAccessEmptyGlowing INSTANCE = new BlockAccessEmptyGlowing();

	@Override
	public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {


		return 15 << 20 | 15 << 4;
	}
}
