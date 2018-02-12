package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileTrashCan;
import com.rwtema.extrautils2.tile.TileTrashCanEnergy;
import com.rwtema.extrautils2.tile.TileTrashCanFluids;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockTrashCan extends XUBlockStaticRotation {
	private String suffix;

	public BlockTrashCan() {
		this("");
	}

	public BlockTrashCan(String suffix) {
		super(Material.ROCK);
		this.suffix = suffix;
		ExtraUtils2.proxy.registerTexture("trashcan", "trashcan_top", "trashcan_bottom");
		this.setHardness(3.5F);
		this.setSoundType(SoundType.STONE);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBox(2 * 0.0625F, 0, 2 * 0.0625F, 1 - 2 * 0.0625F, 1 - 6 * 0.0625F, 1 - 2 * 0.0625F);
		model.addBox(0.0625F, 1 - 6 * 0.0625F, 0.0625F, 1 - 0.0625F, 1 - 2 * 0.0625F, 1 - 0.0625F);
		model.addBox(5 * 0.0625F, 1 - 2 * 0.0625F, 7 * 0.0625F, 1 - 5 * 0.0625F, 1 - 0.0625F, 1 - 7 * 0.0625F);
		model.setTextures("trashcan" + suffix, EnumFacing.DOWN, "trashcan_bottom" + suffix, EnumFacing.UP, "trashcan_top" + suffix);
		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileTrashCan();
	}


	public static class Fluid extends BlockTrashCan {
		public Fluid(){
			super("_fluid");
		}

		@Nonnull
		@Override
		public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
			return new TileTrashCanFluids();
		}
	}

	public static class Energy extends BlockTrashCan {
		public Energy(){
			super("_energy");
		}

		@Nonnull
		@Override
		public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
			return new TileTrashCanEnergy();
		}
	}

}
