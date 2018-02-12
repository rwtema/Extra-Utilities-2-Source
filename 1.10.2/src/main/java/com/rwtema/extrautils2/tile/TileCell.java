package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.power.energy.PublicEnergyWrapper;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.datastructures.ISimpleBitSet;
import com.rwtema.extrautils2.utils.datastructures.LazySideMask;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public abstract class TileCell extends XUTile implements ITickable {
	public XUEnergyStorage storage = registerNBT("energy", createEnergyStorage());

	public NBTSerializable.Int extractLimit = new NBTSerializable.Int(getMaxLimits());
	public NBTSerializable.Int receiveLimit = new NBTSerializable.Int(getMaxLimits());
	public ISimpleBitSet inputFlag = registerNBT("sideFlag", new NBTSerializable.BitsSmall((byte) (63)));
	public ISimpleBitSet outputFlag = registerNBT("sideFlag", new NBTSerializable.BitsSmall((byte) (63)));
	int recievedThisTick = 0;
	int extractedThisTick = 0;

	LazySideMask.HasCap.Energy sideMask = new LazySideMask.HasCap.Energy();

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
		sideMask.invalidateAll();
	}

	protected abstract int getMaxLimits();

	public void resetTick() {
		recievedThisTick = 0;
		extractedThisTick = 0;
	}

	@Override
	public void update() {
		if (!outputFlag.isEmpty() && storage.getEnergyStored() > 0 && extractedThisTick < extractLimit.value) {
			ArrayList<IEnergyStorage> storages = new ArrayList<>();
			for (EnumFacing facing : EnumFacing.values()) {
				if (outputFlag.get(facing.ordinal())) {
					TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
					if (tileEntity != null) {
						IEnergyStorage energyStorage = CapGetter.energyReceiver.getInterface(tileEntity, facing.getOpposite());
						if (energyStorage != null && energyStorage.canReceive()) {
							storages.add(energyStorage);
						}
					}
				}
			}

			if (!storages.isEmpty()) {
				int size = storages.size();
				if (size > 1) {
					int divEnergy = Math.min(storage.getEnergyStored(), extractLimit.value - extractedThisTick) / size;
					if (divEnergy > 0) {
						for (IEnergyStorage energyStorage : storages) {
							sendEnergy(energyStorage, Math.min(divEnergy, storage.getEnergyStored()));
						}
					}
					Collections.shuffle(storages);
				}
				for (IEnergyStorage energyStorage : storages) {
					int toSend = Math.min(storage.getEnergyStored(), extractLimit.value - extractedThisTick) / size;
					if (toSend <= 0) {
						break;
					}
					sendEnergy(energyStorage, toSend);
				}
			}
		}
		resetTick();
	}

	protected void sendEnergy(IEnergyStorage energyStorage, int toSend) {
		int received = energyStorage.receiveEnergy(toSend, false);
		extractedThisTick += storage.extractEnergy(received, false);
	}

	@Nonnull
	protected abstract XUEnergyStorage createEnergyStorage();

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		if (inputFlag.get(facing.ordinal())) {
			if (outputFlag.get(facing.ordinal())) {
				return storage;
			} else {
				return new PublicEnergyWrapper.Receive(storage);
			}
		} else {
			if (outputFlag.get(facing.ordinal())) {
				return new PublicEnergyWrapper.Extract(storage);
			} else {
				return new PublicEnergyWrapper.View(storage);
			}
		}

	}

	public static class SurvivalCell extends TileCell {

		@Override
		protected int getMaxLimits() {
			return 1000;
		}

		@Nonnull
		@Override
		protected XUEnergyStorage createEnergyStorage() {
			return new XUEnergyStorage(100000);
		}


	}

	private class StorageInteraction implements IEnergyStorage {
		boolean canExtract;
		boolean canReceive;

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (!canReceive) return 0;
			int maxPossible = receiveLimit.value - recievedThisTick;
			if (maxPossible <= 0) return 0;

			return storage.receiveEnergy(Math.min(maxReceive, maxPossible), simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!canReceive) return 0;
			int maxPossible = extractLimit.value - extractedThisTick;
			if (maxPossible <= 0) return 0;
			return storage.extractEnergy(Math.min(maxPossible, maxExtract), simulate);
		}

		@Override
		public int getEnergyStored() {
			return storage.getEnergyStored();
		}

		@Override
		public int getMaxEnergyStored() {
			return storage.getMaxEnergyStored();
		}

		@Override
		public boolean canExtract() {
			return canExtract;
		}

		@Override
		public boolean canReceive() {
			return canReceive;
		}
	}
}
