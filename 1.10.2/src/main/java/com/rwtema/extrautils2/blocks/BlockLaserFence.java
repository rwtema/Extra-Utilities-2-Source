package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileLaserBeam;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockLaserFence extends XUBlockStatic {
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileLaserBeam();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, BlockColors blockColors) {

	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBoxI(8, 8, 8, 12, 12, 12, "laser_fence_side");
		for (EnumFacing facing : EnumFacing.values()) {
			model.addBoxI(5, 2, 5, 11, 4, 11, "laser_fence_side").rotateToSide(facing);
			model.addBoxI(6, 0, 6, 10, 2, 10, "laser_fence_side").rotateToSide(facing);

			model.addBoxI(5, 2, 5, 11, 4, 11, "laser_fence_color").rotateToSide(facing).setTint(0);
			model.addBoxI(6, 0, 6, 10, 2, 10, "laser_fence_color").rotateToSide(facing).setTint(0);
		}
		return model;
	}


}
