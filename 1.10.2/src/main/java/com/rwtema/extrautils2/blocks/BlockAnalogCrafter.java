package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileAnalogCrafter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAnalogCrafter extends XUBlockStatic {
	public BlockAnalogCrafter() {
		super(Material.WOOD);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = BoxModel.newStandardBlock("analog_crafter_side");
		model.setTextures(EnumFacing.UP, "analog_crafter");
		model.setTextures(EnumFacing.DOWN, "analog_crafter_bottom");
		return model;
	}


	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileAnalogCrafter();
	}
}
