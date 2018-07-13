package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.jei.ParticlePing;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.*;

public class Radar {
	@SideOnly(Side.CLIENT)
	public static KeyBinding searchForItems;
	static ItemStack lastRenderedStack = StackHelper.empty();

	static {

	}

	@SideOnly(Side.CLIENT)
	public static void init() {
		searchForItems = new KeyBinding("key.xu2.searchforitems", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_T, "Extra Utilities 2");

		ClientRegistry.registerKeyBinding(searchForItems);

		Lang.translate(searchForItems.getKeyDescription(), "Search Nearby Inventories for Item");
		MinecraftForge.EVENT_BUS.register(new Radar());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTooltip(RenderTooltipEvent.Pre event) {
		lastRenderedStack = event.getStack();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderFinish(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			lastRenderedStack = StackHelper.empty();
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyPress(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (Minecraft.getMinecraft().currentScreen != null && Keyboard.getEventKeyState()) {
			if (searchForItems.isActiveAndMatches(Keyboard.getEventKey())) {
				ItemStack stackUnderMouse = getStackUnderMouse();
				if (StackHelper.isNonNull(stackUnderMouse)) {
					NetworkHandler.sendPacketToServer(new PacketPing(stackUnderMouse));
//					event.setCanceled(true);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private ItemStack getStackUnderMouse() {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
			if (slotUnderMouse != null) {
				ItemStack stack = slotUnderMouse.getStack();
				if (StackHelper.isNonNull(stack)) {
					return stack;
				}
			}


		}

//		if (!itemListOverlay.getVisibleStacks().isEmpty()) {
//			ItemStack stackUnderMouse = itemListOverlay.getStackUnderMouse();
//			if (StackHelper.isNonNull(stackUnderMouse)) {
//				return stackUnderMouse;
//			}
//		}

		return lastRenderedStack;
	}

	@NetworkHandler.XUPacket
	public static class PacketPing extends XUPacketClientToServer {
		static final long TIMEOUT = 10;
		static final int RANGE = 16;
		static WeakHashMap<EntityPlayer, Long> timeOutsHandler = new WeakHashMap<>();
		ItemStack stack;
		private EntityPlayer player;

		public PacketPing() {

		}

		public PacketPing(@Nonnull ItemStack stack) {
			this.stack = stack.copy();
			this.stack.setTagCompound(null);
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
				if (player == null || StackHelper.isNull(stack) || stack.getItem() == StackHelper.nullItem()) {
					return;
				}

				World world = player.world;

				long time = world.getTotalWorldTime();
				Long aLong = timeOutsHandler.get(player);
				if (aLong != null) {
					if (time - aLong < TIMEOUT) {
						return;
					}
				}
				timeOutsHandler.put(player, time);

				final int x = (int) Math.round(player.posX);
				final int y = (int) Math.round(player.posY);
				final int z = (int) Math.round(player.posZ);

				Item trueItem = stack.getItem();
				int trueItemDamage = stack.getItemDamage();

				TreeSet<BlockPos> positions = new TreeSet<>(Comparator.comparingDouble(o -> PacketPing.this.getRange(x, y, z, o)));

				for (int cx = x - RANGE; cx <= x + RANGE; cx += 16) {
					for (int cz = z - RANGE; cz <= z + RANGE; cz += 16) {
						BlockPos p = new BlockPos(cx, y, cz);
						if (world.isBlockLoaded(p)) {
							Chunk chunk = world.getChunkFromBlockCoords(p);

							Set<Map.Entry<BlockPos, TileEntity>> entrySet = chunk.getTileEntityMap().entrySet();
							for (Map.Entry<BlockPos, TileEntity> entry : entrySet) {

								if (!PacketPing.this.inRange(x, y, z, entry.getKey()))
									continue;

								TileEntity tile = entry.getValue();

								IItemHandler handler = CapGetter.ItemHandler.getInterface(tile, null);
								if (handler == null) continue;

								for (int i = 0; i < handler.getSlots(); i++) {
									ItemStack stack1 = handler.getStackInSlot(i);
									if (StackHelper.isNull(stack1) || stack1.getItem() != trueItem) continue;

									if (trueItem.getHasSubtypes() && stack1.getItemDamage() != trueItemDamage)
										continue;

									positions.add(entry.getKey());

									if (positions.size() >= PacketPong.MAX_SIZE) {
										positions.pollLast();
									}

								}
							}
						}
					}
				}

				if (!positions.isEmpty()) {
					NetworkHandler.sendPacketToPlayer(new PacketPong(new ArrayList<>(positions)), player);
				}

			};
		}


		public int getRange(int x, int y, int z, BlockPos pos) {
			return Math.abs(pos.getX() - x) + Math.abs(pos.getY() - y) + Math.abs(pos.getZ() - z);
		}

		public boolean inRange(int x, int y, int z, BlockPos pos) {
			return Math.abs(pos.getX() - x) <= RANGE && Math.abs(pos.getY() - y) <= RANGE && Math.abs(pos.getZ() - z) <= RANGE;
		}
	}

	@NetworkHandler.XUPacket
	public static class PacketPong extends XUPacketServerToClient {
		public static final int MAX_SIZE = 30;

		ArrayList<BlockPos> positions;

		public PacketPong() {
			super();
		}

		public PacketPong(ArrayList<BlockPos> positions) {

			this.positions = positions;
		}

		@Override
		public void writeData() throws Exception {
			writeInt(positions.size());
			for (BlockPos position : positions) {
				writeBlockPos(position);
			}
		}

		@Override
		public void readData(EntityPlayer player) {
			int size = readInt();
			positions = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				positions.add(readBlockPos());
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new RunnableClient() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					Minecraft.getMinecraft().player.closeScreen();
					for (BlockPos position : positions) {
						for (int i = 0; i < 20; i++)
							Minecraft.getMinecraft().effectRenderer.addEffect(new ParticlePing(Minecraft.getMinecraft().world, position));
					}
				}
			};
		}
	}
}
