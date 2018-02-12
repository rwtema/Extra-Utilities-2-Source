package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockPowerOverload extends XUBlockStatic {
	public BlockPowerOverload() {
		super(Material.ROCK);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel boxes = new BoxModel();
		boxes.addBoxI(1,1,1,15,15,15,"power_overloader");
		return boxes;
	}
}
