package com.rwtema.extrautils2.entity.timber;

import jline.internal.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class EntityTimber {
	HashMap<BlockPos, FallingBlock> treeTrunk = new HashMap<>();


	public static class FallingBlock {
		@Nonnull
		final IBlockState state;
		@Nullable
		final NBTTagCompound tile_tag;
		@Nullable
		final NBTTagCompound desc_tag;
		@Nullable
		final AxisAlignedBB bounds;
		@Nullable
		List<ItemStack> drops;
		int fragility;

		public FallingBlock(@Nonnull IBlockState state, NBTTagCompound tile_tag, NBTTagCompound desc_tag, AxisAlignedBB bounds) {
			this.state = state;
			this.tile_tag = tile_tag;
			this.desc_tag = desc_tag;
			this.bounds = bounds;
		}
	}
}
