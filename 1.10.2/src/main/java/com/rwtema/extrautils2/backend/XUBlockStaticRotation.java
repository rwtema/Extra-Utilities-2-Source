package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.model.BoxModel;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class XUBlockStaticRotation extends XUBlockStatic {
	public XUBlockStaticRotation(Material materialIn) {
		super(materialIn);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		XUBlockStateCreator.Builder builder = new XUBlockStateCreator.Builder(this).addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH);
		builder = addBuildStateProperties(builder);
		return builder.build();
	}

	protected XUBlockStateCreator.Builder addBuildStateProperties(XUBlockStateCreator.Builder builder) {
		return builder;
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = createBaseModel(state.withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.NORTH));
		model.rotateY(state.getValue(XUBlockStateCreator.ROTATION_HORIZONTAL));
		return model;
	}

	protected abstract BoxModel createBaseModel(IBlockState baseState);

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, placer.getHorizontalFacing());
	}
}
