package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxDoubleSided;
import com.rwtema.extrautils2.backend.model.BoxModel;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public abstract class BlockWardBase extends XUBlockStaticRotation {

	private final int irisColor;
	private final int tex_type;

	public BlockWardBase(int irisColor, int tex_type) {
		super(Material.WOOD);
		this.irisColor = irisColor;
		this.tex_type = tex_type;
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel model = new BoxModel();
		model.addBoxI(7, 0, 7, 9, 7, 9, "ward/ward_stick");
		model.add(new BoxDoubleSided(0, 0, 7 / 16F, 1, 1, 9 / 16F).setTexture("ward/ward_prongs").setInvisible(~((1 << 2) | (1 << 3))).setFlipU(2));
		Box box = model.addBoxI(5, 8, 5, 11, 14, 11).setTextureSides(0, "ward/ward_eye_top_" + tex_type, "ward/ward_eye_top_" + tex_type, "ward/ward_eye_front_" + tex_type, "ward/ward_eye_back_" + tex_type, "ward/ward_eye_side_" + tex_type, "ward/ward_eye_side_" + tex_type);
		box.setFlipU(5);
		box.setFlipV(1);
		if (irisColor >= 0)
			model.addBoxI(5, 8, 5, 11, 14, 11, "ward/ward_iris").setInvisible(~(1 << 2)).setTint(1);
		for (Box box1 : model) {
			box1.noCollide = true;
		}

		model.setLayer(BlockRenderLayer.CUTOUT);
		model.rotateY(2);
		return model;
	}

	@Nonnull
	@Override
	public abstract TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}
