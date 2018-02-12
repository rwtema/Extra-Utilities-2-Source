package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.transfernodes.FacingHelper;
import com.rwtema.extrautils2.utils.CapGetter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class TileCreativeEnergy extends XUTile implements ITickable {
	static final int MAX_SEND = 16777216;
	static final IEnergyStorage INFINIENERGY = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return maxExtract;
		}

		@Override
		public int getEnergyStored() {
			return MAX_SEND;
		}

		@Override
		public int getMaxEnergyStored() {
			return MAX_SEND;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	};

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return INFINIENERGY;
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			for (EnumFacing facing : FacingHelper.randOrders[world.rand.nextInt(12)]) {
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
				if (tileEntity != null) {
					IEnergyStorage storage = CapGetter.energyReceiver.getInterface(tileEntity, facing.getOpposite());
					if (storage != null && storage.canReceive()) {
						storage.receiveEnergy(MAX_SEND, false);
					}
				}
			}
		}
	}
}
