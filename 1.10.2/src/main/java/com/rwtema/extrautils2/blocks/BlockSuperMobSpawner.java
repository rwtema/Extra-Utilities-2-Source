package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileSuperMobSpawner;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockSuperMobSpawner extends XUBlockStatic {
	public BlockSuperMobSpawner() {
		super(Material.ROCK);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return new BoxModel(new Box(0, 0, 0, 1, 1, 1).setTexture("mob_spawner")).setLayer(BlockRenderLayer.TRANSLUCENT);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileSuperMobSpawner();
	}
}
