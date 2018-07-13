package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.machine.TileItemWrapper;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockItemWrapper extends XUBlockStaticRotation {
	public BlockItemWrapper() {
		super(Material.ROCK);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		Lang.translate("Allows items/fluids/energy to be inserted or extracted from a selected item");
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel model = BoxModel.newStandardBlock("extrautils2:machine/machine_base_side");
		model.setLayer(BlockRenderLayer.SOLID);
		model.setTextures(EnumFacing.UP, "extrautils2:machine/machine_base");
		model.setTextures(EnumFacing.DOWN, "extrautils2:machine/machine_base_bottom");
		Box box = model.addBox(0, 0, 0, 1, 1, 1);
		box.layer = BlockRenderLayer.TRANSLUCENT;
		box.setTexture("extrautils2:machine/machine_wrapper");
		for (EnumFacing facing : EnumFacing.values()) {
			if (facing != EnumFacing.SOUTH)
				box.setInvisible(facing);
		}

		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileItemWrapper();
	}
}
