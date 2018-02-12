package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockQuarryProxy extends XUBlockStatic {
	public BlockQuarryProxy() {
		super();
		setLightLevel(1);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this).addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_ALL, EnumFacing.DOWN).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel boxes = BoxModel.newStandardBlock("quantum_quarry_side").setTextures(EnumFacing.DOWN, "quantum_quarry_bottom", EnumFacing.UP, "quantum_quarry_top");
		boxes.rotateToSide(state.getValue(XUBlockStateCreator.ROTATION_ALL));
		return boxes;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	@Nonnull
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileQuarryProxy(state.getValue(XUBlockStateCreator.ROTATION_ALL));
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_ALL, facing.getOpposite());
	}

	@Override
	public boolean canPlaceBlockOnSide(@Nonnull World worldIn, @Nonnull BlockPos pos, EnumFacing side) {
		return worldIn.getTileEntity(pos.offset(side, -1)) instanceof TileQuarry;
	}
}
