package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public abstract class XUBlockFull extends XUBlockStatic {

	boolean rotates;

	public XUBlockFull(Material materialIn) {
		super(materialIn);

	}

	@Override
	public void setBlockState(XUBlockStateCreator creator) {
		super.setBlockState(creator);
		rotates = creator.getProperties().contains(XUBlockStateCreator.ROTATION_HORIZONTAL);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return true;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return true;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return true;
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel boxes = BoxModel.newStandardBlock();
		Box box = boxes.get(0);
		for (EnumFacing side : EnumFacing.values()) {
			box.textureSide[side.getIndex()] = getTexture(state, side);
		}

		if (rotates) {
			boxes.rotateY(state.getValue(XUBlockStateCreator.ROTATION_HORIZONTAL));
		}

		return boxes;
	}

	public abstract String getTexture(IBlockState state, EnumFacing side);
}
