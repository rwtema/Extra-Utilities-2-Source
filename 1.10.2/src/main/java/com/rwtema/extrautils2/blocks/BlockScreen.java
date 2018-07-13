package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.tile.TileScreen;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockScreen extends XUBlockStatic {

	public final static float SIZE = 0.1F;
	public static int maxSize = -1;

	public BlockScreen() {
		super(Material.ROCK);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return (new XUBlockStateCreator.Builder(this))
				.addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBox(0, 0, 0, 1, SIZE - 1 / 512F, 1).setTextureSides("panel_bottom", 0, "panel_bottom", 1, "black_screen");
		model.rotateToSide(state.getValue(XUBlockStateCreator.ROTATION_HORIZONTAL));
		return model;
	}


	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileScreen();
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) facing = placer.getHorizontalFacing().getOpposite();
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, facing.getOpposite());
	}
}
