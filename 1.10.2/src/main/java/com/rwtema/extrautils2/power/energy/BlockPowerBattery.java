package com.rwtema.extrautils2.power.energy;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.utils.Lang;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPowerBattery extends XUBlockStatic {

	{
		EnergyTransfer.init();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return BoxModel.newStandardBlock("transfernodes/battery_side").setTextures(0, "transfernodes/battery_top", 1, "transfernodes/battery_top");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Lang.translate("Stores RF energy for wireless transmission"));
		tooltip.add(Lang.translate("Requires RF transmitters to send energy"));
		tooltip.add(Lang.translateArgs("Max Storage: %s RF", TilePowerBattery.ENERGY_CAPACITY));
		tooltip.add(Lang.translateArgs("Requires: %s GP", TilePowerBattery.ENERGY_REQUIREMENT));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	@Nonnull
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TilePowerBattery();
	}
}
