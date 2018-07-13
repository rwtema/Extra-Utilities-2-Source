package com.rwtema.extrautils2.power.energy;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockPowerTransmitter extends XUBlockStatic {
	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this).addWorldProperties(XUBlockStateCreator.ROTATION_ALL).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBoxI(4, 0, 4, 12, 2, 12);
		model.addBoxI(2, 0, 7, 14, 1, 9);
		model.addBoxI(7, 0, 2, 9, 1, 14);
		model.setTextures("transfernodes/transmitter_side", 0, "transfernodes/transmitter_bottom", "transfernodes/transmitter_top");
		model.rotateToSide(state.getValue(XUBlockStateCreator.ROTATION_ALL));
		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	@Nonnull
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TilePowerTransmitter();
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_ALL, facing.getOpposite());
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Lang.translateArgs("Transmits RF energy from batteries to nearby blocks"));
		tooltip.add(Lang.translateArgs("Max Transfer: %s RF/T", EnergyTransfer.MAX_TRANSFER));
		tooltip.add(Lang.translateArgs("Range: %s blocks", TilePowerTransmitter.RANGE));
		tooltip.add(Lang.translateArgs("Requires: %s GP", TilePowerBattery.ENERGY_REQUIREMENT));
	}
}
