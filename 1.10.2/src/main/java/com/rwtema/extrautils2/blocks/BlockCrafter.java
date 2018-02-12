package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileCrafter;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCrafter extends XUBlockStatic {

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = BoxModel.newStandardBlock("autocraft_side");
		model.setTextures(EnumFacing.UP, "autocraft");
		model.setTextures(EnumFacing.DOWN, "interact_back");
		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileCrafter();
	}


}
