package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileCreativeEnergy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockCreativeEnergy extends XUBlockStatic {
	public BlockCreativeEnergy() {
		super();
		setBlockUnbreakable();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return BoxModel.newStandardBlock("creative_energy");
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileCreativeEnergy();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}
