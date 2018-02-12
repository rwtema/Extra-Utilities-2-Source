package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileCreativeChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockCreativeChest extends XUBlockStaticRotation {
	public BlockCreativeChest() {
		super(Material.ROCK);
		setBlockUnbreakable();
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel boxes = new BoxModel();
		boxes.addBoxI(1, 0, 1, 15, 14, 15, "chest_creative_side").setTextureSides(0, "chest_creative", 1, "chest_creative", 3, "chest_creative_front");
		boxes.addBoxI(6, 6, 15, 10, 11, 16, "chest_creative_side").setTextureSides(0, "chest_creative", 1, "chest_creative", 3, "chest_creative_front");
		return boxes;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileCreativeChest();
	}
}
