package com.rwtema.extrautils2.power.energy;


import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.power.IPowerSubType;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.Collection;

public class TilePowerBattery extends TilePower implements IPowerSubType {
	public static final int ENERGY_REQUIREMENT = 4;
	public static final int ENERGY_CAPACITY = 3200;
	public XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(ENERGY_CAPACITY));
	PublicEnergyWrapper.Receive receive = new PublicEnergyWrapper.Receive(energy) {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return gridReceivesEnergy(maxReceive, simulate);
		}
	};

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return receive;
	}

	private int gridReceivesEnergy(int maxReceive, boolean simulate) {
		int received = energy.receiveEnergy(maxReceive, simulate);
		if (received < maxReceive) {
			PowerManager.PowerFreq freq = PowerManager.instance.getPowerFreq(frequency);
			Collection<TilePowerBattery> batteries = freq.getSubTypes(EnergyTransfer.ENERGY_SYSTEM_STORAGE_KEY);
			if (batteries != null) {
				for (TilePowerBattery battery : batteries) {
					if (battery != this && (simulate || battery.isLoaded())) {
						received += battery.energy.receiveEnergy(maxReceive - received, simulate);
						if (received == maxReceive)
							return received;
					}
				}
			}
		}

		return received;
	}

	@Override
	public float getPower() {
		return ENERGY_REQUIREMENT;
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public Collection<ResourceLocation> getTypes() {
		return EnergyTransfer.ENERGY_SYSTEM_STORAGE;
	}


	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			ITextComponent chat = Lang.chat("Stored Energy (Battery): %s / %s RF", energy.getEnergyStored(), ENERGY_CAPACITY);

			PowerManager.PowerFreq freq = PowerManager.instance.getPowerFreq(frequency);
			Collection<TilePowerBattery> batteries = freq.getSubTypes(EnergyTransfer.ENERGY_SYSTEM_STORAGE_KEY);
			if (batteries != null) {
				int t = 0;
				for (TilePowerBattery battery : batteries) {
					t += battery.energy.getEnergyStored();
				}
				chat = chat.appendText("\n")
						.appendSibling(Lang.chat("Stored Energy (Total): %s / %s RF", t, batteries.size() * ENERGY_CAPACITY));
			}

			SpecialChat.sendChat(playerIn, chat);
		}
		return true;
	}

}
