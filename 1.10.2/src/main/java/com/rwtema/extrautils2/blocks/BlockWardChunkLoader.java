package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.tile.TileChunkLoader;
import com.rwtema.extrautils2.tile.TilePower;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockWardChunkLoader extends BlockWardBase {
	public BlockWardChunkLoader() {
		super(-1, 1);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileChunkLoader();
	}
}
