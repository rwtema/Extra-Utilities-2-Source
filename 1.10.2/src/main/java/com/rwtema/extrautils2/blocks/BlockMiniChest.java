package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileMinChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockMiniChest extends XUBlockStaticRotation {

	public BlockMiniChest() {
		super(Material.WOOD);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel model = new BoxModel();
		model.addBoxI(5,0,5,11,6,11, "minichest_side").setTextureSides(0, "minichest_bottom", 1, "minichest_top", 3, "minichest_front");
		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileMinChest();
	}
}
