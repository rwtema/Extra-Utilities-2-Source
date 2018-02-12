package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	private static final TIntObjectHashMap<IDynamicHandler> customDynamicGuiMap = new TIntObjectHashMap<>();

	public static int register(String name, IDynamicHandler handler) {
		int id =  name.hashCode() | 0x80000000;

		if(customDynamicGuiMap.containsKey(id))
			throw new RuntimeException("Duplicate id: " + id + " - " + customDynamicGuiMap.get(id) + " - (adding " + handler + ")");
		customDynamicGuiMap.put(id, handler);
		return id;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (customDynamicGuiMap.containsKey(ID)) {
			return customDynamicGuiMap.get(ID).getDynamicContainer(ID, player, world, x, y, z);
		}

		if (ID == -1) {
			ItemStack heldItem = player.getHeldItemMainhand();
			if (StackHelper.isNull(heldItem)) return null;
			Item item = heldItem.getItem();
			if (item instanceof IGuiHandler)
				return ((IGuiHandler) item).getServerGuiElement(ID, player, world, x, y, z);
			if (item instanceof IDynamicHandler)
				return ((IDynamicHandler) item).getDynamicContainer(ID, player, world, x, y, z);
		} else {
			BlockPos pos = new BlockPos(x, y, z);
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof IGuiHandler)
				return ((IGuiHandler) tileEntity).getServerGuiElement(ID, player, world, x, y, z);
			if (tileEntity instanceof IDynamicHandler)
				return ((IDynamicHandler) tileEntity).getDynamicContainer(ID, player, world, x, y, z);
			IBlockState state = world.getBlockState(pos);
			if (state instanceof IGuiHandler)
				return ((IGuiHandler) state).getServerGuiElement(ID, player, world, x, y, z);
			if (state instanceof IDynamicHandler)
				return ((IDynamicHandler) state).getDynamicContainer(ID, player, world, x, y, z);
			Block block = state.getBlock();
			if (block instanceof IGuiHandler)
				return ((IGuiHandler) block).getServerGuiElement(ID, player, world, x, y, z);
			if (block instanceof IDynamicHandler)
				return ((IDynamicHandler) block).getDynamicContainer(ID, player, world, x, y, z);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (customDynamicGuiMap.containsKey(ID)) {
			return new DynamicGui(customDynamicGuiMap.get(ID).getDynamicContainer(ID, player, world, x, y, z));
		}

		if (ID == -1) {
			ItemStack heldItem = player.getHeldItemMainhand();
			if (StackHelper.isNull(heldItem)) return null;
			Item item = heldItem.getItem();
			if (item instanceof IGuiHandler)
				return ((IGuiHandler) item).getClientGuiElement(ID, player, world, x, y, z);
			if (item instanceof IDynamicHandler)
				return new DynamicGui(((IDynamicHandler) item).getDynamicContainer(ID, player, world, x, y, z));
		} else {
			BlockPos pos = new BlockPos(x, y, z);
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof IGuiHandler)
				return ((IGuiHandler) tileEntity).getClientGuiElement(ID, player, world, x, y, z);
			if (tileEntity instanceof IDynamicHandler)
				return new DynamicGui(((IDynamicHandler) tileEntity).getDynamicContainer(ID, player, world, x, y, z));
			IBlockState state = world.getBlockState(pos);
			if (state instanceof IGuiHandler)
				return ((IGuiHandler) state).getClientGuiElement(ID, player, world, x, y, z);
			if (state instanceof IDynamicHandler)
				return new DynamicGui(((IDynamicHandler) state).getDynamicContainer(ID, player, world, x, y, z));
			Block block = state.getBlock();
			if (block instanceof IGuiHandler)
				return ((IGuiHandler) block).getClientGuiElement(ID, player, world, x, y, z);
			if (block instanceof IDynamicHandler)
				return new DynamicGui(((IDynamicHandler) block).getDynamicContainer(ID, player, world, x, y, z));
		}
		return null;
	}

	@NetworkHandler.XUPacket
	public static class PacketOpenGui extends XUPacketClientToServer {

		int id;
		private EntityPlayer player;

		public PacketOpenGui() {
			super();
		}

		public PacketOpenGui(int id) {
			this.id = id;
		}

		@Override
		public void writeData() throws Exception {
			writeInt(id);
		}

		@Override
		public void readData(EntityPlayer player) {
			id = readInt();
			this.player = player;
		}

		@Override
		public Runnable doStuffServer() {
			return new Runnable() {
				@Override
				public void run() {
					player.openGui(ExtraUtils2.instance, id, player.world, 0, 0, 0);
				}
			};
		}
	}
}
