package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.state.IBlockState;

public class BlockCell extends XUBlockStatic {


	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = BoxModel.newStandardBlock("cell_basic");
		final float f = 1 / 1024F;
		model.addBox(f, f, f, 1 - f, 1 - f, 1 - f, "minecraft:redstone_block");
		return model;
	}
}
