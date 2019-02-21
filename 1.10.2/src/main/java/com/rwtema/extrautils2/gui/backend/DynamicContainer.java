package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ISidedFunction;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class DynamicContainer extends Container {
	public static final int playerInvWidth = 162;
	public static final int centerX = (playerInvWidth + 8) / 2;
	public static final int centerSlotX = (playerInvWidth + 8 - 18) / 2;
	public static final int playerInvHeight = 95;
	public static final ISidedFunction<String, Integer> STRING_WIDTH_FUNCTION = new ISidedFunction<String, Integer>() {
		@Override
		@SideOnly(Side.SERVER)
		public Integer applyServer(String input) {
			return input != null ? input.length() : 0;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Integer applyClient(String input) {
			return input != null ? Minecraft.getMinecraft().fontRenderer.getStringWidth(input) : 0;
		}
	};
	public static final ISidedFunction<Pair<String, Integer>, Integer> STRING_HEIGHTS = new ISidedFunction<Pair<String, Integer>, Integer>() {

		@Override
		public Integer applyServer(Pair<String, Integer> input) {
			int w = 0;
			String[] split = input.getKey().split("\n");
			for (String s : split) {
				float sw = STRING_WIDTH_FUNCTION.applyServer(s);
				w += MathHelper.ceil(sw / w);
			}

			return w * 9;
		}

		@Override
		public Integer applyClient(Pair<String, Integer> input) {
			List<String> strings = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(input.getKey(), input.getValue());
			return strings.size() * 9;
		}
	};
	public static final ResourceLocation texBackground = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/gui_base.png");
	public static final ResourceLocation texBackgroundBlack = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/gui_base_black.png");
	public static final ResourceLocation texBackgroundIndentation = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/gui_base_indent.png");
	public static final ResourceLocation texBackgroundBorder = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/gui_base_invisible.png");


	private final static ItemStack genericItemStack = new ItemStack(Blocks.COBBLESTONE, 65);
	public final HashSet<Slot> playerSlots = new HashSet<>();
	public int playerSlotsStart = -1;
	public LinkedList<EntityPlayerMP> entityPlayerMPs = new LinkedList<>();
	public int width = 176, height = 166;
	public boolean changesOnly = false;
	public boolean isClient = false;
	ByteBuf previousPacket = Unpooled.buffer();
	boolean fixed = false;
	private LinkedHashMultimap<DynamicWindow, IWidget> windowWidgets = LinkedHashMultimap.create();
	private List<IWidget> widgets = new ArrayList<>();
	private List<IWidgetKeyInput> widgetKeyInputs = new ArrayList<>();
	private List<IWidgetMouseInput> widgetMouseInputs = new ArrayList<>();
	private List<IWidgetClientNetwork> widgetReceivers = new ArrayList<>();
	private List<IWidgetServerNetwork> widgetNetworks = new ArrayList<>();
	private List<IWidgetClientTick> widgetClientTick = new ArrayList<>();
	private HashMap<IWidget, DynamicWindow> windowOwner = new HashMap<>();
	private HashSet<DynamicWindow> windows = new HashSet<>();

	public DynamicContainer() {

	}

	public List<IWidgetClientTick> getWidgetClientTick() {
		return widgetClientTick;
	}

	public HashMap<IWidget, DynamicWindow> getWindowOwner() {
		return windowOwner;
	}

	public LinkedHashMultimap<DynamicWindow, IWidget> getWindowWidgets() {
		return windowWidgets;
	}

	public List<IWidgetKeyInput> getWidgetKeyInputs() {
		return ImmutableList.copyOf(widgetKeyInputs);
	}

	public List<IWidgetMouseInput> getWidgetMouseInputs() {
		return ImmutableList.copyOf(widgetMouseInputs);
	}

	public List<IWidget> getWidgets() {
		return ImmutableList.copyOf(widgets);
	}

	void addSlot(Slot slot) {
		this.addSlotToContainer(slot);
	}

	@Override
	public void addListener(IContainerListener listener) {
		if (listener instanceof EntityPlayerMP) {
			entityPlayerMPs.add((EntityPlayerMP) listener);
		}

		changesOnly = false;
		super.addListener(listener);
	}

	@Override
	public void removeListener(@Nonnull IContainerListener listener) {
		if (listener instanceof EntityPlayerMP) {
			entityPlayerMPs.remove(listener);
		}
		super.removeListener(listener);
	}

	@Override
	public void detectAndSendChanges() {
		if (isClient) return;
		super.detectAndSendChanges();

		ByteBuf buffer = Unpooled.buffer();
		XUPacketBuffer packetBuffer = new XUPacketBuffer(buffer);
		for (IWidgetServerNetwork widget : widgetNetworks) {
			widget.addToDescription(packetBuffer);
		}

		previousPacket.setIndex(0, previousPacket.writerIndex());
		if (ByteBufUtil.equals(buffer, previousPacket))
			return;

		previousPacket = Unpooled.copiedBuffer(buffer);


		for (EntityPlayerMP player : entityPlayerMPs) {
			NetworkHandler.sendPacketToPlayer(new PacketGUIData(this.windowId, packetBuffer), player);
		}

	}

	protected void validate() {
		boolean flag = false;
		for (IWidget widget : widgets) {
			widget.addToContainer(this);
			if (!flag && widget instanceof ITransferPriority) {
				flag = true;
			}
		}
		fixed = true;

		if (flag) {
			inventorySlots.sort((o1, o2) -> {
				int p1 = o1 instanceof ITransferPriority ? ((ITransferPriority) o1).getTransferPriority() : 0;
				int p2 = o2 instanceof ITransferPriority ? ((ITransferPriority) o2).getTransferPriority() : 0;

				int compare = -Integer.compare(p1, p2);
				if (compare != 0) return compare;

				return Integer.compare(o1.slotNumber, o2.slotNumber);
			});

			for (int i = 0; i < inventorySlots.size(); i++) {
				Slot inventorySlot = inventorySlots.get(i);
				inventorySlot.slotNumber = i;
			}
		}
	}

	public void addPlayerSlotsToBottom(IInventory inventory) {
		addPlayerSlots(inventory, (this.width - playerInvWidth) / 2, this.height - playerInvHeight);
	}

	public void crop() {
		crop(4);
	}

	public void crop(int border) {
		int maxX = 18;
		int maxY = 18;

		for (IWidget widget : windowWidgets.get(null)) {
			maxX = Math.max(maxX, widget.getX() + widget.getW());
			maxY = Math.max(maxY, widget.getY() + widget.getH());
		}

		this.width = maxX + border;
		this.height = maxY + border;
	}

	public void cropForPlayerSlots() {
		crop(4);
		if (this.width < playerInvWidth + 8) {
			this.width = playerInvWidth + 8;
		}
	}

	public void cropAndAddPlayerSlots(InventoryPlayer inventory) {
		crop(4);
		this.height += DynamicContainer.playerInvHeight;

		if (this.width < playerInvWidth + 8) {
			this.width = playerInvWidth + 8;
		}

		addPlayerSlotsToBottom(inventory);
	}

	public void addPlayerSlots(IInventory inventory, int x, int y) {
		this.playerSlotsStart = 0;

		for (IWidget widget : this.widgets) {
			if (widget instanceof Slot) {
				playerSlotsStart++;
			}
		}

		addWidget(new WidgetTextTranslate(x, y, inventory.getName(), DynamicContainer.playerInvWidth));

		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				WidgetSlot w = new WidgetSlot(inventory, k + j * 9 + 9, x + k * 18, y + 14 + j * 18);
				playerSlots.add(w);
				addWidget(w);
			}
		}

		for (int j = 0; j < 9; ++j) {
			WidgetSlot w = new WidgetSlot(inventory, j, x + j * 18, y + 14 + 58);
			playerSlots.add(w);
			addWidget(w);
		}
	}

	public void addWidget(IWidget w) {
		addWidget(w, null);
	}

	public void addWidgetClientTick(IWidgetClientTick w) {
		widgetClientTick.add(w);
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		for (IWidget widget : widgets) {
			widget.onContainerClosed(this, playerIn);
		}
	}

	public void addWidget(IWidget w, DynamicWindow dynamicWindow) {
		if (fixed) throw new IllegalStateException();
		this.widgets.add(w);

		if (dynamicWindow != null) {
			windows.add(dynamicWindow);
		}

		windowWidgets.put(dynamicWindow, w);
		windowOwner.put(w, dynamicWindow);

		if (w instanceof IWidgetKeyInput) {
			widgetKeyInputs.add((IWidgetKeyInput) w);
		}
		if (w instanceof IWidgetMouseInput) {
			widgetMouseInputs.add((IWidgetMouseInput) w);
		}
		if (w instanceof IWidgetClientNetwork) {
			widgetReceivers.add((IWidgetClientNetwork) w);
		}
		if (w instanceof IWidgetServerNetwork) {
			widgetNetworks.add((IWidgetServerNetwork) w);
		}
		if (w instanceof IWidgetClientTick) {
			widgetClientTick.add((IWidgetClientTick) w);
		}

		if (w instanceof IAdditionalWidgets) {
			for (IWidget iWidget : ((IAdditionalWidgets) w).getAdditionalWidgets()) {
				addWidget(iWidget, dynamicWindow);
			}
		}
	}

	@Override
	@ItemStackNonNull
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		ItemStack itemstack = StackHelper.empty();
		Slot slot = this.inventorySlots.get(par2);

		if (slot instanceof SlotCrafting) {
			ItemStack itemstack1 = slot.getStack();
			if (slot.getHasStack() && StackHelper.isNonNull(itemstack1)) {
				itemstack = itemstack1.copy();
				if (!this.mergeItemStack(itemstack1, playerSlotsStart, this.inventorySlots.size(), true)) {
					return StackHelper.empty();
				}

				slot.onSlotChange(itemstack1, itemstack);

				if (StackHelper.isEmpty(itemstack1)) {
					slot.putStack(StackHelper.empty());
				} else {
					slot.onSlotChanged();
				}

				if (StackHelper.getStacksize(itemstack1) == StackHelper.getStacksize(itemstack)) {
					return StackHelper.empty();
				}

				CompatHelper.setSlot(slot, par1EntityPlayer, itemstack1);
			}
			return itemstack;
		}

		if (playerSlotsStart > 0 && slot != null && slot.getHasStack()) {
			ItemStack otherItemStack = slot.getStack();
			if (StackHelper.isNull(otherItemStack)) return StackHelper.empty();
			itemstack = otherItemStack.copy();

			if (par2 < playerSlotsStart) {
				if (!this.mergeItemStack(otherItemStack, playerSlotsStart, this.inventorySlots.size(), true)) {
					return StackHelper.empty();
				}
			} else {
				if (!this.mergeItemStack(otherItemStack, 0, playerSlotsStart, false)) {
					return StackHelper.empty();
				}
			}

			if (StackHelper.isEmpty(otherItemStack)) {
				slot.putStack(StackHelper.empty());
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

//	@SuppressWarnings("unchecked")
//	@ContainerSectionCallback
//	public Map<ContainerSection, List<Slot>> getSlots() {
//		return InventoryTweaksHelper.getSlots(this, false);
//	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
		boolean flag = false;

		if (stack.isStackable()) {
			for (int i = reverseDirection ? endIndex - 1 : startIndex;
				 reverseDirection && i >= startIndex || !reverseDirection && i < endIndex;
				 i += reverseDirection ? -1 : 1) {

				Slot slot = this.inventorySlots.get(i);

				if (!isValidForMerging(slot))
					continue;

				ItemStack currentSlotStack = slot.getStack();

				if (StackHelper.isEmpty(currentSlotStack) || !ItemHandlerHelper.canItemStacksStack(stack, currentSlotStack) || !slot.isItemValid(stack)) {
					continue;
				}

				int j = StackHelper.getStacksize(currentSlotStack) + StackHelper.getStacksize(stack);
				int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

				if (j <= maxSize) {
					StackHelper.setStackSize(stack, 0);
					slot.putStack(ItemHandlerHelper.copyStackWithSize(currentSlotStack, j));
					slot.onSlotChanged();
					flag = true;
					break;
				} else if (maxSize - StackHelper.getStacksize(currentSlotStack) > 0) {
					StackHelper.decrease(stack, maxSize - StackHelper.getStacksize(currentSlotStack));
					slot.putStack(ItemHandlerHelper.copyStackWithSize(currentSlotStack, maxSize));
					slot.onSlotChanged();
					flag = true;
				}
			}
		}

		if (StackHelper.isEmpty(stack)) {
			return flag;
		}

		for (int i = reverseDirection ? endIndex - 1 : startIndex;
			 reverseDirection && i >= startIndex || !reverseDirection && i < endIndex;
			 i += reverseDirection ? -1 : 1) {

			Slot slot = this.inventorySlots.get(i);

			if (!isValidForMerging(slot))
				continue;

			if (slot.getHasStack() || !StackHelper.isEmpty(slot.getStack()) || !slot.isItemValid(stack)) {
				continue;
			}

			if (StackHelper.getStacksize(stack) > slot.getSlotStackLimit()) {
				int toRemove = Math.min(slot.getSlotStackLimit(), StackHelper.getStacksize(stack));
				ItemStack otherStack = stack.copy();
				StackHelper.setStackSize(otherStack, toRemove);
				StackHelper.decrease(stack, toRemove);
				slot.putStack(otherStack);
			} else {
				slot.putStack(StackHelper.safeCopy(stack));
				StackHelper.setStackSize(stack, 0);
			}

			slot.onSlotChanged();
			flag = true;
			break;
		}

		return flag;
	}

	public boolean isValidForMerging(Slot slot) {
		return !(slot instanceof WidgetCraftingMatrix.WidgetSlotIngredients) && !(slot instanceof WidgetCraftingMatrix.WidgetSlotCrafting);
	}

	public int getStringWidth(String text) {
		return ExtraUtils2.proxy.apply(STRING_WIDTH_FUNCTION, text);
	}

	public void addTitle(String name) {
		addTitle(Lang.translate(name), false);
	}

	public void addTitle(XUTile tile) {
		addTitle(tile.getBlockState());
	}

	public void addTitle(XUBlockState blockState) {
		addTitle(new ItemStack(blockState.getBlock(), 1, blockState.dropMeta).getDisplayName());
	}

	public void addTitle(String name, boolean translate) {
		WidgetText e;
		if (translate) {
			e = new WidgetTextTranslate(5, 5, name, getStringWidth(I18n.translateToLocal(name)));
		} else {
			e = new WidgetText(5, 5, name, getStringWidth(name));
		}
		addWidget(e);
	}

	@Override
	public ItemStack slotClick(int slotId, int clickedButton, ClickType clickTypeIn, EntityPlayer playerIn) {
		if (slotId >= 0 && slotId < inventorySlots.size()) {
			Slot slot = this.inventorySlots.get(slotId);
			if (slot instanceof ISlotClick) {
				return ((ISlotClick) slot).slotClick(this, slotId, clickedButton, clickTypeIn, playerIn);
			}
		}
		return super.slotClick(slotId, clickedButton, clickTypeIn, playerIn);
	}

	@SideOnly(Side.CLIENT)
	public void sendInputPacket(IWidgetClientNetwork widget, XUPacketBuffer buffer) {
		int i = widgetReceivers.indexOf(widget);
		if (i < 0) throw new RuntimeException("Unable to find widget");
		NetworkHandler.sendPacketToServer(new PacketGUIInput(windowId, i, buffer));
	}

	public HashSet<DynamicWindow> getWindows() {
		return windows;
	}

	public void onSlotChanged(int index) {
//		Slot slot = inventorySlots.get(index);
		inventoryItemStacks.set(index, genericItemStack);
//		for (EntityPlayerMP playerMP : entityPlayerMPs) {
//			playerMP.sendSlotContents(this, index, slot.createStack());
//			playerMP.updateHeldItem();
//		}
	}

	@SideOnly(Side.CLIENT)
	public void loadGuiDimensions(DynamicGui dynamicGui) {
		dynamicGui.setWidthHeight(width, height);
	}

	@SideOnly(Side.CLIENT)
	public boolean drawBackgroundOverride(DynamicGui gui) {
		return false;
	}

	@NetworkHandler.XUPacket
	public static class PacketGUIInput extends XUPacketClientToServer {
		private int windowId;
		private int widgetId;
		private XUPacketBuffer packetBuffer;
		private EntityPlayer player;

		public PacketGUIInput() {
			super();
		}

		public PacketGUIInput(int windowId, int widgetId, XUPacketBuffer packetBuffer) {
			this.windowId = windowId;
			this.widgetId = widgetId;
			this.packetBuffer = packetBuffer;
		}

		@Override
		public void writeData() throws Exception {
			writeInt(windowId);
			writeShort(widgetId);
			if (packetBuffer == null)
				writeInt(0);
			else
				writePacketBuffer(packetBuffer);
		}

		@Override
		public void readData(EntityPlayer player) {
			this.player = player;
			windowId = readInt();
			widgetId = readUnsignedShort();
			packetBuffer = readPacketBuffer();
		}

		@Override
		public Runnable doStuffServer() {
			return new Runnable() {
				@Override
				public void run() {
					Container openContainer = player.openContainer;
					if (windowId == 0 || openContainer.windowId != windowId || !openContainer.canInteractWith(player))
						return;

					DynamicContainer dynamicContainer = (DynamicContainer) openContainer;
					List<IWidgetClientNetwork> receivers = dynamicContainer.widgetReceivers;
					if (widgetId < 0 || widgetId >= receivers.size()) return;
					receivers.get(widgetId).receiveClientPacket(packetBuffer);
				}
			};
		}
	}

	@NetworkHandler.XUPacket
	public static class PacketSetGhost extends XUPacketClientToServer {
		private int windowId;
		private EntityPlayer player;
		private int slot;
		private ItemStack stack;

		public PacketSetGhost(int windowId, int slot, ItemStack stack) {
			this.windowId = windowId;
			this.slot = slot;
			this.stack = stack;
		}

		public PacketSetGhost() {

		}

		@Override
		public void readData(EntityPlayer player) {
			this.player = player;
			windowId = readInt();
			slot = readInt();
			stack = readItemStack();
		}

		@Override
		public void writeData() throws Exception {
			writeInt(windowId);
			writeInt(slot);
			writeItemStack(stack);
		}

		@Override
		public Runnable doStuffServer() {
			return new Runnable() {
				@Override
				public void run() {
					Container openContainer = player.openContainer;
					if (windowId == 0 || openContainer.windowId != windowId || !openContainer.canInteractWith(player))
						return;

					DynamicContainer dynamicContainer = (DynamicContainer) openContainer;
					List<IWidget> widgets1 = dynamicContainer.widgets;
					IWidget iWidget = widgets1.get(slot);
					if (iWidget instanceof WidgetSlotGhost) {
						((WidgetSlotGhost) iWidget).putStack(stack);
					}
				}
			};
		}
	}

	@NetworkHandler.XUPacket
	public static class PacketGUIData extends XUPacketServerToClient {
		private int windowId;
		private XUPacketBuffer packetBuffer;

		public PacketGUIData() {
			super();
		}

		public PacketGUIData(int windowId, XUPacketBuffer packetBuffer) {
			this.windowId = windowId;
			this.packetBuffer = packetBuffer;
		}

		@Override
		public void writeData() throws Exception {
			writeInt(windowId);
			writePacketBuffer(packetBuffer);
		}

		@Override
		public void readData(EntityPlayer player) {
			windowId = readInt();
			packetBuffer = readPacketBuffer();

		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new Runnable() {
				@Override
				public void run() {
					Container openContainer = Minecraft.getMinecraft().player.openContainer;
					if (windowId == 0 || openContainer.windowId != windowId) return;

					DynamicContainer dynamicContainer = (DynamicContainer) openContainer;
					for (IWidgetServerNetwork widget : dynamicContainer.widgetNetworks) {
						widget.handleDescriptionPacket(packetBuffer);
					}
				}
			};
		}
	}


}

