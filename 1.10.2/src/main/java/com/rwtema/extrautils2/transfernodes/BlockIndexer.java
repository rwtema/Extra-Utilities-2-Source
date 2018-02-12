package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TilePower;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockIndexer extends XUBlockStaticRotation {
	public BlockIndexer() {
		super(Material.ROCK);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		return BoxModel.newStandardBlock("panel_indexer_side").setTextures(EnumFacing.SOUTH, "panel_indexer_front");
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileIndexer();
	}
}
