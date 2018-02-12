package com.rwtema.extrautils2.power.energy;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import gnu.trove.iterator.TIntObjectIterator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EnergyTransfer {
	public static final ResourceLocation ENERGY_SYSTEM_STORAGE_KEY = new ResourceLocation(ExtraUtils2.MODID, "EnergySystemStorage");
	public static final Collection<ResourceLocation> ENERGY_SYSTEM_STORAGE = ImmutableList.of(ENERGY_SYSTEM_STORAGE_KEY);

	public static final ResourceLocation ENERGY_SYSTEM_TRANSMITTER_KEY = new ResourceLocation(ExtraUtils2.MODID, "EnergySystemTransmitters");
	public static final Collection<ResourceLocation> ENERGY_SYSTEM_TRANSMITTERS = ImmutableList.of(ENERGY_SYSTEM_TRANSMITTER_KEY);

	public static final int MAX_TRANSFER = 80;

	public static final int REBUILD_TIME = 20 * 5;
	static final EnumFacing[] sidesPlusNull = new EnumFacing[]{
			null,
			EnumFacing.DOWN,
			EnumFacing.UP,
			EnumFacing.NORTH,
			EnumFacing.SOUTH,
			EnumFacing.WEST,
			EnumFacing.EAST
	};
	public static int tick = 0;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(EnergyTransfer.class);
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) return;
		tick++;
		if (tick >= REBUILD_TIME) {
			tick = 0;
		}

		TIntObjectIterator<PowerManager.PowerFreq> iterator = PowerManager.instance.frequencyHolders.iterator();
		while (iterator.hasNext()) {
			iterator.advance();
			PowerManager.PowerFreq freq = iterator.value();

			if (!freq.isPowered()) continue;

			Collection<TilePowerBattery> storages = freq.getSubTypes(ENERGY_SYSTEM_STORAGE_KEY);
			if (storages == null || storages.isEmpty()) continue;

			Collection<TilePowerTransmitter> transmitters = freq.getSubTypes(ENERGY_SYSTEM_TRANSMITTER_KEY);
			if (transmitters == null || transmitters.isEmpty()) continue;

			int totalEnergyAvailable = 0;
			for (TilePowerBattery storage : storages) {
				if (!storage.isInvalid())
					totalEnergyAvailable += storage.energy.getEnergyStored();
			}

			if (totalEnergyAvailable == 0) continue;

			HashMultimap<World, BlockPos> targets = HashMultimap.create();

			for (TilePowerTransmitter transmitter : transmitters) {
				if (transmitter.isInvalid()) continue;
				World world = transmitter.getWorld();
				if (world == null) continue;
				List<BlockPos> targetList = transmitter.getTargets(tick == 0);
				if (targetList == null) continue;
				targets.putAll(world, targetList);
			}

			if (targets.isEmpty()) continue;

			WeakLinkedSet<IEnergyStorage> receivers = new WeakLinkedSet<>();
			for (World world : targets.keySet()) {
				Set<BlockPos> blockPoses = targets.get(world);
				for (BlockPos pos : blockPoses) {
					if (world.isBlockLoaded(pos)) {
						TileEntity tile = world.getTileEntity(pos);
						if (isValidOutput(tile)) {
							for (EnumFacing facing : sidesPlusNull) {
								IEnergyStorage storage = CapGetter.energyReceiver.getInterface(tile, facing);
								if (storage != null) {
									receivers.add(storage);
									if (facing == null) break;
								}
							}
						}
					}
				}
			}

			if (receivers.isEmpty()) return;

			EnumFacing[] facings = EnumFacing.values();

			int transfer = Math.min(MAX_TRANSFER, totalEnergyAvailable) / receivers.size();
			int toExtract = sendPower(totalEnergyAvailable, receivers, facings, transfer);
			if (receivers.size() > 1)
				toExtract = toExtract + sendPower(totalEnergyAvailable - toExtract, receivers, facings, MAX_TRANSFER - transfer);

			for (TilePowerBattery storage : storages) {
				if (!storage.isInvalid()) {
					toExtract -= storage.energy.extractEnergy(toExtract, false);
					if (toExtract == 0) break;
				}
			}
		}
	}

	private static int sendPower(int totalEnergyAvailable, Collection<IEnergyStorage> receivers, EnumFacing[] facings, int maxTransfer) {
		int sentPower = 0;

		for (IEnergyStorage receiver : receivers) {
			if (totalEnergyAvailable <= 0) break;

			int sendAvailable = Math.min(maxTransfer, totalEnergyAvailable);

			if (sendAvailable > 0) {
				if (receiver.canReceive()) {
					int i = receiver.receiveEnergy(sendAvailable, false);
					totalEnergyAvailable -= i;
					sentPower += i;
				}
			}
		}
		return sentPower;
	}

	public static boolean isValidOutput(TileEntity tile) {
		return !(tile instanceof TilePowerBattery) && CapGetter.energyReceiver.hasInterface(tile, null);
	}
}
