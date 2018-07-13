package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileLargishChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockLargishChest extends XUBlockStaticRotation {
	public BlockLargishChest() {
		super(Material.WOOD);
		setHardness(2.5F);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel model = BoxModel.newStandardBlock("large_chest");
		model.setTextures(2, "large_chest_side");
		model.setTextures(3, "large_chest_front");
		model.setTextures(4, "large_chest_side");
		model.setTextures(5, "large_chest_side");
		return model;
	}


	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileLargishChest();
	}
}
