package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileTrashChest;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTrashChest extends XUBlockStaticRotation {
	public BlockTrashChest() {
		super(Material.ROCK);
		this.setHardness(3.5F);
		this.setSoundType(SoundType.STONE);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel boxes = new BoxModel();
		boxes.addBoxI(1, 0, 1, 15, 14, 15, "trashchest_side").setTextureSides(0, "trashchest_bottom", 1, "trashchest_top", 3, "trashchest_front");
		boxes.addBoxI(6, 6, 15, 10, 11, 16, "trashchest_side").setTextureSides(0, "trashchest_bottom", 1, "trashchest_top", 3, "trashchest_front");
		boxes.addBoxI(5, 14, 7, 11, 15, 9, "trashchest_side").setTextureSides(0, "trashchest_bottom", 1, "trashchest_top");
		return boxes;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileTrashChest();
	}
}
