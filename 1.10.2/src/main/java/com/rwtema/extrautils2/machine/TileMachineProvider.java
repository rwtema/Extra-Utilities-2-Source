package com.rwtema.extrautils2.machine;

import com.rwtema.extrautils2.power.energy.PublicEnergyWrapper;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import com.rwtema.extrautils2.utils.CapGetter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;

public class TileMachineProvider extends TileMachine  {

	PublicEnergyWrapper.Extract extract = new PublicEnergyWrapper.Extract(storage);

	@Override
	public void process() {
		switch (redstone_state.value) {
			case OPERATE_REDSTONE_ON:
				if (!powered.value) {
					setInactive();
					return;
				}
				break;
			case OPERATE_REDSTONE_OFF:
				if (powered.value) {
					setInactive();
					return;
				}
				break;
			case OPERATE_REDSTONE_PULSE:
				if (pulses.value == 0) {
					setInactive();
					return;
				}
				break;
		}

		sendEnergy();

		if (!active || machine.getRunError(world, pos, this, speed) != null) {
			setInactive();
			return;
		}

		int n = 1 + upgrades.getLevel(Upgrade.SPEED);
		for (int i = 0; i < n; i++) {
			if (totalTime > 0) {
				int energyAtTime1 = (int) ((float) energyOutput * processTime / (float) totalTime);
				int energyAtTime2 = (int) (((float) energyOutput * Math.min(totalTime, processTime + speed)) / (float) totalTime);
				if (!processEnergy(energyAtTime2 - energyAtTime1)) {
					setInactive();
					if((world.getTotalWorldTime() % ROLLBACK_INTERVAL) == 0){
						processTime += 1;

						if (processTime >= totalTime) {
							processTime = 0;
							totalTime = 0;
							energyOutput = 0;
						}
					}
					return;
				}

				setActive();

				if (i == 0) {
					machine.processingTick(this, curRecipe, processTime, n);
				}

				processTime += speed;

				if (processTime >= totalTime) {
					processTime = 0;
					totalTime = 0;
					energyOutput = 0;
				}
			} else {
				processRecipeInput();

				if (curRecipe == null) {
					setInactive();
					return;
				}

				totalTime = curRecipe.getProcessingTime(itemInputMap, fluidInputMap);
				energyOutput = curRecipe.getEnergyOutput(itemInputMap, fluidInputMap);

				processTime = 0;

				if (totalTime == 0 && !processEnergy(energyOutput)) {
					setInactive();
					return;
				}

				setActive();
				consumeInputs();

				if (pulses.value > 0) {
					pulses.value--;
				}
			}
		}
	}

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return machine != null ? extract : null;
	}


	public void sendEnergy() {
		if (storage.getEnergyStored() <= 0) return;

		int maxSend = Math.min(machine.energyTransferLimit * (1 + upgrades.getLevel(Upgrade.SPEED)), storage.getEnergyStored());

		if (maxSend <= 0) return;

		int sent = 0;

		LinkedHashSet<IEnergyStorage> receivers = new LinkedHashSet<>();

		for (EnumFacing facing : FacingHelper.randOrders[world.rand.nextInt(12)]) {
			TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
			if (tileEntity != null) {
				IEnergyStorage storage = CapGetter.energyReceiver.getInterface(tileEntity, facing.getOpposite());
				if (storage != null)
					receivers.add(storage);
			}
		}

		if (receivers.isEmpty()) return;

		int toSend = maxSend / receivers.size();
		if (toSend > 0) {
			for (IEnergyStorage receiver : receivers) {
				int energy = receiver.receiveEnergy(toSend, false);
				maxSend -= energy;
				storage.modifyEnergyStored(-energy);
				if (maxSend <= 0) break;
			}
		}

		for (IEnergyStorage receiver : receivers) {
			int energy = receiver.receiveEnergy(maxSend, false);
			maxSend -= energy;
			storage.modifyEnergyStored(-energy);
			if (maxSend <= 0) break;
		}

		markDirty();
	}

	protected boolean processEnergy(int amount) {
		if (amount == 0) return true;
		int curStored = storage.receiveEnergy(amount, true);
		if (curStored == amount) {
			storage.receiveEnergy(amount, false);
			return true;
		} else {

			return false;
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		nbt.setInteger("TotalTime", totalTime);
		nbt.setInteger("EnergyOutput", energyOutput);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		totalTime = nbt.getInteger("TotalTime");
		energyOutput = nbt.getInteger("EnergyOutput");
	}
}
