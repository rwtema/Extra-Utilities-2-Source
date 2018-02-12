package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.tile.TileResonator;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockResonator extends XUBlockStatic {
	public BlockResonator() {
		super(Material.ROCK);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		final BoxModel boxes = new BoxModel();
		boxes.addBoxI(0,0,0,16,15,16, "resonator_side").setTextureSides(0, "resonator_bottom", 1, "resonator_top");
		return boxes;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileResonator();
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}
}
