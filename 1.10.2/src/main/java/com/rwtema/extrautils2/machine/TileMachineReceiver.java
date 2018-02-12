package com.rwtema.extrautils2.machine;

import com.rwtema.extrautils2.power.energy.PublicEnergyWrapper;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class TileMachineReceiver extends TileMachine  {
	PublicEnergyWrapper.Receive receive = new PublicEnergyWrapper.Receive(storage);

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


		if (!active || machine.getRunError(world, pos, this, speed) != null) {
			setInactive();
			return;
		}

		int n = (1 + upgrades.getLevel(Upgrade.SPEED));
		for (int i = 0; i < n; i++) {
			processRecipeInput();

			if (curRecipe == null) {
				setInactive();
				processTime = 0;
				return;
			}

			totalTime = curRecipe.getProcessingTime(itemInputMap, fluidInputMap);
			energyOutput = curRecipe.getEnergyOutput(itemInputMap, fluidInputMap);


			boolean success;
			if (totalTime <= 0) {
				success = processEnergy(energyOutput);
			} else {
				int energyAtTime1 = (int) ((float) energyOutput * processTime / (float) totalTime);
				int energyAtTime2 = (int) (((float) energyOutput * Math.min(totalTime, processTime + speed)) / (float) totalTime);
				success = processEnergy(energyAtTime2 - energyAtTime1);
			}

			if (!success) {
				if (processTime > 0 && (world.getTotalWorldTime() % ROLLBACK_INTERVAL) == 0) {
					processTime--;
				}
				setInactive();
				return;
			}

			setActive();

			if (i == 0) {
				machine.processingTick(this, curRecipe, processTime, n);
			}

			processTime += speed;

			if (processTime >= totalTime) {
				processTime = 0;
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
		return machine != null ? receive : null;
	}


	protected boolean processEnergy(int amount) {
		if (storage.extractEnergy(amount, true) != amount)
			return false;
		storage.extractEnergy(amount, false);
		return true;
	}


}
