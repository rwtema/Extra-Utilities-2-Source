package com.rwtema.extrautils2.power;

import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NetworkHandler.XUPacket
public class PacketPowerInfo extends XUPacketBase {
	BlockPos pos;
	float energy = -1;
	float efficiency = -1;
	private EntityPlayer player;

	public PacketPowerInfo() {
		super();
	}

	public PacketPowerInfo(BlockPos pos) {
		this.pos = pos;
	}

	public PacketPowerInfo(float energy, float efficiency, BlockPos pos) {
		this.energy = energy;
		this.efficiency = efficiency;
		this.pos = pos;
	}

	@Override
	public void writeData() throws Exception {
		writeBlockPos(pos);
		writeFloat(energy);
		writeFloat(efficiency);
	}

	@Override
	public void readData(EntityPlayer player) {
		this.player = player;
		pos = readBlockPos();
		energy = readFloat();
		efficiency = readFloat();
	}

	@Override
	public Runnable doStuffServer() {
		return new Runnable() {
			@Override
			public void run() {
				if (!(player instanceof EntityPlayerMP)) return;
				TileEntity tile = player.world.getTileEntity(pos);
				if (!(tile instanceof IPower)) return;

				int freq = Freq.getBasePlayerFreq((EntityPlayerMP) player);

				IPower power = (IPower) tile;

				float v;
				float v2;
				int frequency = power.frequency();
				if (PowerManager.areFreqOnSameGrid(freq, frequency)) {
					v = PowerManager.getCurrentPower(power);
					v2 = PowerManager.getEfficiency(power);
				} else {
					v = Float.NaN;
					v2 = 1;
				}

				if (v == energy && v2 == efficiency) return;
				NetworkHandler.sendPacketToPlayer(new PacketPowerInfo(v, v2, pos), player);
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Runnable doStuffClient() {
		return new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				if ((ClientPower.currentPosition == pos) || (ClientPower.currentPosition != null && ClientPower.currentPosition.equals(pos))) {
					ClientPower.currentPositionEnergy = energy;
					ClientPower.currentPositionEfficiency = efficiency;
				}
			}
		};
	}

	@Override
	public boolean isValidSenderSide(Side properSenderSide) {
		return true;
	}
}
