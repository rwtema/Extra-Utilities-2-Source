package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.WeakHashMap;

public class TileClock extends TilePower implements ITickable {

	public static final long DAY_LENGTH = 24000L;
	public NBTSerializable.NBTBoolean powered = registerNBT("powered", new NBTSerializable.NBTBoolean(false));
	public NBTSerializable.NBTBoolean moving = registerNBT("moving", new NBTSerializable.NBTBoolean(false));
	public NBTSerializable.Int targetTime = registerNBT("target_time", new NBTSerializable.Int(-1));
	public XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(1000000));

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float getPower() {
		return 0;
	}

	int SPEED = 24 * 12;

	@Override
	public void update() {
		if (moving.value ) {

		}
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		powered.writeToPacket(packet);
		moving.writeToPacket(packet);
		targetTime.writeToPacket(packet);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		powered.readFromPacket(packet);
		moving.readFromPacket(packet);
		targetTime.readFromPacket(packet);
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
		powered.value = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
	}


	public static class TimeHandler {
		private class Ticker {

		}

		WeakHashMap<World, Ticker > data = new WeakHashMap<>();
	}

	@NetworkHandler.XUPacket
	public static class TimeHandlerPacket extends XUPacketServerToClient {

		public TimeHandlerPacket() {

		}

		@Override
		public void writeData() throws Exception {

		}

		@Override
		public void readData(EntityPlayer player) {

		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {

				}
			};
		}
	}
}
