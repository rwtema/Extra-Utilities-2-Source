package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public interface ILeftClickHandler {

	boolean leftClick(World world, EntityPlayer player, ItemStack stack);

	@NetworkHandler.XUPacket
	public static class PacketSendLeftClick extends XUPacketClientToServer {
		static {
			MinecraftForge.EVENT_BUS.register(PacketSendLeftClick.class);
		}

		private ItemStack stack = StackHelper.empty();
		private EntityPlayer player;

		public PacketSendLeftClick() {
			super();
		}

		public PacketSendLeftClick(ItemStack stack) {
			this.stack = stack;
		}

		@SubscribeEvent
		public static void handleLeftBlock(PlayerInteractEvent.LeftClickBlock event) {
			ItemStack stack = event.getItemStack();
			if (StackHelper.isNonNull(stack) && stack.getItem() instanceof ILeftClickHandler) {
				ILeftClickHandler leftClickHandler = (ILeftClickHandler) stack.getItem();
				if (leftClickHandler.leftClick(event.getWorld(), event.getEntityPlayer(), event.getItemStack())) {
					event.setCanceled(true);
					if (event.getWorld().isRemote) {
						NetworkHandler.sendPacketToServer(new PacketSendLeftClick(stack));
					}
				}
			}
		}

		@SubscribeEvent
		public static void handleLeftEmpty(PlayerInteractEvent.LeftClickEmpty event) {
			ItemStack stack = event.getItemStack();
			if (StackHelper.isNonNull(stack) && stack.getItem() instanceof ILeftClickHandler) {
				ILeftClickHandler leftClickHandler = (ILeftClickHandler) stack.getItem();
				if (leftClickHandler.leftClick(event.getWorld(), event.getEntityPlayer(), event.getItemStack())) {
					if (event.getWorld().isRemote) {
						NetworkHandler.sendPacketToServer(new PacketSendLeftClick(stack));
					}
				}
			}
		}

		@Override
		public void writeData() throws Exception {
			writeItemStack(stack);
		}

		@Override
		public void readData(EntityPlayer player) {
			this.player = player;
			stack = readItemStack();
		}

		@Override
		public Runnable doStuffServer() {
			return () -> {
				ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
				if (ItemStack.areItemStacksEqual(heldItem, stack)) {
					if (stack.getItem() instanceof ILeftClickHandler) {
						((ILeftClickHandler) stack.getItem()).leftClick(player.world, player, stack);
					}
				}
			};
		}
	}
}
