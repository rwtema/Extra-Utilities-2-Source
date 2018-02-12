package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockFull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class BlockAngelBlock extends XUBlockFull {
	public BlockAngelBlock() {
		super(Material.ROCK);
	}

	@Override
	public String getTexture(IBlockState state, EnumFacing side) {
		return "angel_block";
	}
}
