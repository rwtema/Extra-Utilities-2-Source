package com.rwtema.extrautils2.transfernodes;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.PublicWrapper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.PositionPool;
import com.rwtema.extrautils2.utils.blockaccess.ThreadSafeBlockAccess;
import com.rwtema.extrautils2.utils.client.GLStateAttributes;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.datastructures.ItemRefComparator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class TileIndexer extends TilePower implements ITickable, IDynamicHandler, IPipeConnect {
	public final static int COUNTDOWN_SINGLE = 10;
	public TObjectIntHashMap<ItemRef> countDownTotal = new TObjectIntHashMap<>();
	public HashMultimap<ItemRef, SidedPos> itemRefPosEntryHashMultimap = HashMultimap.create();
	public List<SidedPos> positions = new ArrayList<>();
	public TObjectIntHashMap<ItemRef> counts = new TObjectIntHashMap<>();
	public TObjectIntHashMap<ItemRef> countDown = new TObjectIntHashMap<>();
	public TObjectIntHashMap<ItemRef> orders = new TObjectIntHashMap<>();
	public ItemStackHandler stacks = registerNBT("stacks", new ItemStackHandler(9));
	public long positionsHash;
	public long itemsHash;
	public ListenableFuture<Runnable> submit;
	public boolean dirty;
	public ItemStackHandler returnStacks = registerNBT("return", new ItemStackHandler(9) {
		@Override
		protected void onContentsChanged(int slot) {
			TileIndexer.this.markDirty();
			dirty = true;
		}
	});
	private IItemHandler publicHandler = new PublicWrapper.Extract(returnStacks);

	public void reload() {
		if (world.isRemote) return;
		if (!active || submit != null) return;

		final PositionPool pool = new PositionPool();
		final HashSet<BlockPos> alreadyChecking = new HashSet<>();
		final LinkedList<BlockPos> toCheck = new LinkedList<>();
		BlockPos intern = pool.intern(pos);

		boolean adjacentPipe = false;


		final ArrayList<SidedPos> sides = new ArrayList<>();

		alreadyChecking.add(intern);
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos offset = pool.offset(intern, facing);
			adjacentPipe = adjacentPipe || TransferHelper.getPipe(world, offset) != null;

			alreadyChecking.add(offset);
			toCheck.add(offset);
		}

		if (!adjacentPipe) {
			return;
		}

		final IBlockAccess world = new ThreadSafeBlockAccess((WorldServer) this.world);

		submit = HttpUtil.DOWNLOADER_EXECUTOR.submit(
				new Callable<Runnable>() {
					@Override
					public Runnable call() throws Exception {


						long hash = alreadyChecking.hashCode() * 31 + toCheck.hashCode();
						BlockPos pos1;
						while ((pos1 = toCheck.poll()) != null) {
							if (!active || TileIndexer.this.isInvalid()) return null;
							hash = hash * 31 + pos1.hashCode() + 1;
							IPipe pipe = TransferHelper.getPipe(world, pos1);
							if (pipe == null) continue;

							hash = hash * 31 + pipe.hashCode();

							for (EnumFacing facing : EnumFacing.values()) {
								if (pipe.hasCapability(world, pos1, facing, CapGetter.ItemHandler)) {
									sides.add(new SidedPos(pos1, facing));
									hash = (hash * 31 + pos1.hashCode()) * 31 + facing.ordinal();
								}

								BlockPos offset = pool.offset(pos1, facing);
								if (alreadyChecking.contains(offset))
									continue;

								hash = hash * 31 + facing.ordinal();

								IPipe otherPipe = TransferHelper.getPipe(world, offset);

								if (otherPipe != null &&
										(pipe.canOutput(world, pos1, facing, null) && otherPipe.canInput(world, offset, facing.getOpposite()) ||
												(pipe.canInput(world, pos1, facing) && otherPipe.canOutput(world, offset, facing.getOpposite(), null)))) {
									hash = hash * 31 + offset.hashCode();
									alreadyChecking.add(offset);
									toCheck.add(offset);
								}
							}
						}

						final long finalHash = hash;

						return () -> TileIndexer.this.process(sides, finalHash);
					}
				});

		Futures.addCallback(submit, new FutureCallback<Runnable>() {
			@Override
			public void onSuccess(Runnable result) {
				if (result != null)
					FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(result);
				TileIndexer.this.submit = null;
			}

			@Override
			public void onFailure(@Nonnull final Throwable t) {
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
					throw Throwables.propagate(t);
				});
				TileIndexer.this.submit = null;
			}
		});
	}

	private void process(List<SidedPos> sidedPositions, long finalHash) {
		if (positionsHash == finalHash) return;
		positionsHash = finalHash;

		process(sidedPositions);
	}

	private void process(List<SidedPos> sidedPositions) {
		long h = 0;

		if (!isLoaded() || world.isRemote) return;

		HashMultimap<ItemRef, SidedPos> itemRefPosEntryHashMultimap = HashMultimap.create();
		TObjectIntHashMap<ItemRef> counts = new TObjectIntHashMap<>();

		Interner<ItemRef> interner = Interners.newStrongInterner();
		for (ItemRef itemRef : this.counts.keySet()) {
			interner.intern(itemRef);
		}

		HashSet<IItemHandler> processedHandlers = new HashSet<>();

		for (SidedPos sidedPos : sidedPositions) {
			IPipe pipe = TransferHelper.getPipe(world, sidedPos.pos);
			if (pipe == null) continue;

			IItemHandler handler = pipe.getCapability(world, sidedPos.pos, sidedPos.side, CapGetter.ItemHandler);
			if (handler == null || processedHandlers.contains(handler)) continue;

			processedHandlers.add(handler);

			h = h * 31 + sidedPos.hashCode();
			h = h * 31 + handler.getSlots();

			for (int i = 0; i < handler.getSlots(); i++) {
				ItemStack stack = handler.extractItem(i, 64, true);
				if (StackHelper.isNull(stack)) continue;

				ItemRef itemRef = interner.intern(ItemRef.wrap(stack));
				if (itemRef == ItemRef.NULL)
					continue;

				itemRefPosEntryHashMultimap.put(itemRef, sidedPos);
				counts.adjustOrPutValue(itemRef, StackHelper.getStacksize(stack), StackHelper.getStacksize(stack));

				h = h * 31 + i;
				h = h * 31 + itemRef.hashCode();
				h = h * 31 + StackHelper.getStacksize(stack);

			}
		}


		this.positions = sidedPositions;
		this.itemRefPosEntryHashMultimap = itemRefPosEntryHashMultimap;
		this.counts = counts;
		this.itemsHash = h;
	}

	@Override
	public void onPowerChanged() {
		if (!active) {
			if (submit != null) {
				submit.cancel(true);
			}
		}
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return publicHandler;
	}

	@Override
	public float getPower() {
		return 8;
	}

	@Override
	public void update() {
		if (itemRefPosEntryHashMultimap == null || (world.getTotalWorldTime() % 200 == 0)) {
			reload();
		}

		if (!countDown.isEmpty()) {
			TObjectIntIterator<ItemRef> iterator = countDown.iterator();
			while (iterator.hasNext()) {
				iterator.advance();
				int value = iterator.value();
				value--;
				if (value == 0) {
					ItemRef key = iterator.key();
					int i = orders.get(key);
					if (i <= 0) {
						iterator.remove();
						orders.remove(key);
					} else if (i == 1) {
						iterator.remove();
						orders.remove(key);
						retrieve(key, 1);
						dirty = true;
					} else if (i > 1) {
						if (retrieve(key, 1)) {
							iterator.setValue(Math.max(COUNTDOWN_SINGLE, countDownTotal.get(key)));
							orders.put(key, i - 1);
						} else {
							iterator.remove();
							countDownTotal.remove(key);
							orders.remove(key);
						}
						dirty = true;

					}
				} else {
					iterator.setValue(value);
				}
			}


		}

		if (dirty) {
			process();
			reload();
		}
	}

	public void process() {
		process(positions);
	}

	private boolean retrieve(ItemRef key, int order) {
		HashMultimap<ItemRef, SidedPos> itemRefPosEntryHashMultimap = this.itemRefPosEntryHashMultimap;
		Set<SidedPos> sidedPositions = itemRefPosEntryHashMultimap.get(key);


		order = InventoryHelper.getMaxInsert(stacks, key.createItemStack(order));


		if (order <= 0) return false;

		boolean inserted = false;

		HashSet<IItemHandler> processedHandlers = new HashSet<>();

		for (SidedPos sidedPos : sidedPositions) {
			IItemHandler handler;
			if (sidedPos.pos.equals(pos)) {
				TileEntity tileEntity = world.getTileEntity(pos.offset(sidedPos.side));
				if (tileEntity == null) continue;

				handler = CapGetter.ItemHandler.getInterface(tileEntity, sidedPos.side.getOpposite());
			} else {
				IPipe pipe = TransferHelper.getPipe(world, sidedPos.pos);
				if (pipe == null) continue;

				handler = pipe.getCapability(world, sidedPos.pos, sidedPos.side, CapGetter.ItemHandler);
			}

			if (handler == null || !processedHandlers.add(handler)) continue;

			for (int i = 0; i < handler.getSlots(); i++) {
				ItemStack stack = handler.extractItem(i, order, true);
				if (StackHelper.isNull(stack)) continue;
				if (key.equalsItemStack(stack)) {
					int toOrder = InventoryHelper.getMaxInsert(stacks, stack);
					if (toOrder == 0) return inserted;
					inserted = true;
					ItemStack extracted = handler.extractItem(i, toOrder, false);
					InventoryHelper.insert(stacks, extracted, false);
					order -= toOrder;

					if (order <= 0) return true;
				}
			}
		}

		return inserted;

	}

	public void order(ItemRef ref, boolean b, int speed) {
		int order;
		if (b) {
			order = ref.getMaxStackSize();
		} else
			order = 1;

		if (orders.containsKey(ref)) {
			int i = orders.get(ref);
			order = Math.min(order + i, ref.getMaxStackSize());
		}

		orders.put(ref, order);


		int countdownSingle = COUNTDOWN_SINGLE * speed;
		if (countDownTotal.containsKey(ref)) {
			countdownSingle = Math.min(countdownSingle, countDownTotal.get(ref));
		}

		if (countDown.get(ref) <= 0) {
			countDown.put(ref, countdownSingle);
		}

		countDownTotal.put(ref, countdownSingle);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		positionsHash = 0;
		process();
		reload();
		return new ContainerIndexer(this, player, 1);
	}

	@Override
	public boolean forceConnect(EnumFacing side) {
		return true;
	}

	public static class SidedPos {
		@Nonnull
		public final EnumFacing side;

		@Nonnull
		public final BlockPos pos;

		public SidedPos(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
			this.pos = pos;
			this.side = side;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SidedPos sidedPos = (SidedPos) o;

			return side == sidedPos.side && pos.equals(sidedPos.pos);
		}

		@Override
		public int hashCode() {
			return 31 * side.hashCode() + pos.hashCode();
		}
	}

	public static class ContainerIndexer extends DynamicContainerTile {
		static final int HORIZONTAL_SLOTS = 9;
		static final int VERTICAL_SLOTS = 4;
		public static JeiTextInterface textInterface = new JeiTextInterface();
		public final EntityPlayer player;
		final private int speed;
		private final WidgetTextInput widgetTextInput;
		public ArrayList<ItemRef> list = new ArrayList<>();
		public WidgetItemRefButton mainButton;
		List<ItemRef> filteredList = null;
		String prevFilter = "";
		WidgetScrollBar scrollBar;
		private TileIndexer tileIndexer;

		protected ContainerIndexer() {
			super(new TileIndexer());
			player = null;
			speed = 0;
			widgetTextInput = null;
		}

		public ContainerIndexer(final TileIndexer tileIndexer, EntityPlayer player, int speed) {
			super(tileIndexer);

			this.player = player;
			this.speed = speed;


			scrollBar = new WidgetScrollBar(4 + HORIZONTAL_SLOTS * 18 + 4, 13 + 4, VERTICAL_SLOTS * 18, 0, 0);
			addWidget(scrollBar);

			addWidget(new WidgetRawData() {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeBoolean(tileIndexer.active);
				}

				@Override
				public void handleDescriptionPacket(XUPacketBuffer packet) {
					tileIndexer.active = packet.readBoolean();
				}
			});

			crop();

			addTitle(Lang.translate("Indexer"));

			widgetTextInput = new WidgetTextInput(width - 90 - 4, 4, 90, textInterface.getFilterText()) {
				@Override
				protected void onValueChanged() {
//					textInterface.setFilterText(getText());
				}
			};
			addWidget(widgetTextInput);

			addWidgetClientTick(new IWidgetClientTick() {
				String prevValue = textInterface.getFilterText();

				@Override
				@SideOnly(Side.CLIENT)
				public void updateClient() {
					String filterText = textInterface.getFilterText();
					if (prevValue.equals(filterText)) {
						String widgetTextInputText = widgetTextInput.getText();
						if (!widgetTextInputText.equals(filterText)) {
							textInterface.setFilterText(widgetTextInputText);
							prevValue = filterText;
							filteredList = null;
						}
					} else {
						widgetTextInput.setText(filterText);
						prevValue = filterText;
						filteredList = null;
					}
				}
			});


			addWidget(new WidgetRawData() {
				@Override
				public void addToDescription(final XUPacketBuffer packet) {
					TObjectIntHashMap<ItemRef> countDown = tileIndexer.countDown;
					packet.writeInt(countDown.size());
					countDown.forEachEntry((a, b) -> {
						a.write(packet);
						packet.writeInt(b);
						packet.writeInt(Math.max(COUNTDOWN_SINGLE, tileIndexer.countDownTotal.get(a)));
						return true;
					});
				}

				@Override
				public void handleDescriptionPacket(XUPacketBuffer packet) {
					int n = packet.readInt();
					if (n == 0) {
						if (!tileIndexer.countDown.isEmpty())
							tileIndexer.countDown.clear();
						return;
					}

					TObjectIntHashMap<ItemRef> countDown = new TObjectIntHashMap<>(n);
					TObjectIntHashMap<ItemRef> countDownTotal = new TObjectIntHashMap<>(n);
					for (int i = 0; i < n; i++) {
						ItemRef ref = ItemRef.read(packet);
						countDown.put(ref, packet.readInt());
						countDownTotal.put(ref, packet.readInt());

					}
					tileIndexer.countDown = countDown;
					tileIndexer.countDownTotal = countDownTotal;
				}
			});

			addWidget(new WidgetRawData() {
				long lastSentHash = 0;

				@Override
				public void addToDescription(XUPacketBuffer packet) {
					if (tileIndexer.itemsHash == lastSentHash) {
						packet.writeBoolean(true);
						return;
					}
					tileIndexer.itemsHash = lastSentHash;
					packet.writeBoolean(false);

					TObjectIntHashMap<ItemRef> counts = tileIndexer.counts;
					packet.writeInt(counts.size());
					TObjectIntIterator<ItemRef> iterator = counts.iterator();
					while (iterator.hasNext()) {
						iterator.advance();
						iterator.key().write(packet);
						packet.writeInt(iterator.value());
					}
				}

				@Override
				public void handleDescriptionPacket(XUPacketBuffer packet) {
					if (packet.readBoolean()) return;
					TObjectIntHashMap<ItemRef> items = new TObjectIntHashMap<>();
					TreeSet<ItemRef> refTreeSet = new TreeSet<>(ItemRefComparator.names);
					int n = packet.readInt();
					for (int i = 0; i < n; i++) {
						ItemRef itemRef = ItemRef.read(packet);
						refTreeSet.add(itemRef);
						int count = packet.readInt();
						items.put(itemRef, count);
					}

					tileIndexer.counts = items;
					list = Lists.newArrayList(refTreeSet);
					filteredList = null;
				}
			});

			for (int yi = 0; yi < VERTICAL_SLOTS; yi++) {
				for (int xi = 0; xi < HORIZONTAL_SLOTS; xi++) {
					int i = yi * HORIZONTAL_SLOTS + xi;
					WidgetItemRefButton w = new WidgetItemRefButton(i, 4 + xi * 18, 4 + 13 + yi * 18);
					if (i == 0) {
						mainButton = w;
					}
					addWidget(w);
				}
			}

			crop();

			int left = (width - 9 * 18) / 2;

			DynamicWindow sideWindow = new DynamicWindow(DynamicWindow.WindowSide.LEFT);
			WidgetCraftingMatrix matrix = new WidgetCraftingMatrix(player, 4, 4, 3, 3);
			for (IWidget widget : matrix.widgets) {
				addWidget(widget, sideWindow);
			}

			for (int xi = 0; xi < 9; xi++) {
				addWidget(new WidgetSlotItemHandler(tileIndexer.stacks, xi, left + xi * 18, height) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return false;
					}
				});
			}

			for (int xi = 0; xi < 9; xi++) {
				addWidget(new WidgetSlotItemHandler(tileIndexer.returnStacks, xi, left + xi * 18, height + 27));
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
			this.tileIndexer = tileIndexer;
		}


		public static class JeiTextInterface {

			@Nonnull
			public String getFilterText() {
				return "";
			}

			public void setFilterText(@Nonnull String text) {

			}
		}

		public class WidgetItemRefButton extends WidgetClickBase implements IWidgetClientNetwork, IWidgetCustomJEIIngredient {

			private final int i;

			public WidgetItemRefButton(int i, int x, int y) {
				super(x, y, 18, 18);
				this.i = i;
			}

			@SideOnly(Side.CLIENT)
			public ItemRef getRef() {
				if (!tileIndexer.active) return ItemRef.NULL;
				int j = this.i + scrollBar.scrollValue * HORIZONTAL_SLOTS;
				if (j < 0) return ItemRef.NULL;
				List<ItemRef> list = ContainerIndexer.this.list;

				String filterText = widgetTextInput.getText().toLowerCase();
				if (!prevFilter.equals(filterText) || filteredList == null) {
					prevFilter = filterText;
					if (filterText.equals("")) {
						filteredList = list;
					} else {
						String[] split = filterText.split(" ");

						filteredList = list.stream().filter(s -> {
									ItemStack itemStack = s.createItemStack(1);
									if (StackHelper.isNull(itemStack)) return false;
									List<String> tooltip = CompatHelper112.getTooltip(itemStack, player, false);

									mainLoop:
									for (String s1 : split) {
										for (String s2 : tooltip) {
											if (s2.toLowerCase().contains(s1)) continue mainLoop;
										}

										return false;
									}

									return true;
								}

						).collect(Collectors.toList());
					}
					scrollBar.setValues(0, Math.max(0, filteredList.size() / HORIZONTAL_SLOTS - VERTICAL_SLOTS + 1));
				}

				if (j < filteredList.size()) {
					return filteredList.get(j);
				}
				return ItemRef.NULL;
			}

			@Override
			public void receiveClientPacket(XUPacketBuffer buffer) {
				ItemRef read = ItemRef.read(buffer);
				if (read == ItemRef.NULL) return;
				boolean b = buffer.readBoolean();
				tileIndexer.order(read, b, speed);
			}

			@Override
			@SideOnly(Side.CLIENT)
			public XUPacketBuffer getPacketToSend(int mouseButton) {
				XUPacketBuffer pkt = new XUPacketBuffer();
				ItemRef ref = getRef();
				if (ref == ItemRef.NULL) return null;
				ref.write(pkt);
				pkt.writeBoolean(GuiContainer.isShiftKeyDown());
				return pkt;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				ItemRef ref = getRef();
				if (ref != ItemRef.NULL) {

					int i = tileIndexer.counts.get(ref);
					String s = i <= 0 ? "0" : ("" + i);


					GlStateManager.enableLighting();
					RenderHelper.enableGUIStandardItemLighting();
					gui.renderSmallStackText(ref.createItemStack(1), s, guiLeft + getX() + 1, guiTop + getY() + 1);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.disableLighting();
				}

				float t;

				if (tileIndexer.active) {
					int i1 = tileIndexer.countDown.get(ref);

					if (i1 <= 0) return;

					float j = Math.max(COUNTDOWN_SINGLE, tileIndexer.countDownTotal.get(ref));

					t = (((float) i1) / j) % 1;
				} else {
					t = 1;
				}

				if (t <= 0) {
					return;
				}

				GLStateAttributes states = GLStateAttributes.loadStates();

				GlStateManager.pushMatrix();
				{
					GlStateManager.translate(guiLeft + getX(), guiTop + getY(), 300);
					GlStateManager.enableBlend();
					GlStateManager.disableDepth();
					GlStateManager.disableAlpha();
					GlStateManager.disableTexture2D();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					GlStateManager.color(0, 0, 0, 0.5F);
					Tessellator instance = Tessellator.getInstance();
					IVertexBuffer tess = CompatClientHelper.wrap(instance.getBuffer());
					tess.begin(7, DefaultVertexFormats.POSITION);

					double angle = Math.PI / 2 + t * Math.PI * 2;
					float dx = (float) Math.cos(angle);
					float dy = (float) Math.sin(angle);

					float adx = Math.abs(dx);
					float ady = Math.abs(dy);

					if (adx < ady) {
						dx = dx / ady;
						dy = Math.signum(dy);
					} else {
						dy = dy / adx;
						dx = Math.signum(dx);
					}
					dx = (1 + dx) / 2 * 18;
					dy = 18 - ((1 + dy) / 2 * 18);

					if (t <= 0.125) {
						tess.pos(9, 0, 0).endVertex();
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
					} else if (t <= 0.375) {
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
						tess.pos(9, 0, 0).endVertex();
						tess.pos(0, 0, 0).endVertex();
					} else if (t <= 0.625) {
						tess.pos(0, 18, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
						tess.pos(9, 0, 0).endVertex();
						tess.pos(0, 0, 0).endVertex();
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
						tess.pos(0, 18, 0).endVertex();
					} else if (t <= 0.875) {
						tess.pos(0, 18, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
						tess.pos(9, 0, 0).endVertex();
						tess.pos(0, 0, 0).endVertex();

						tess.pos(0, 18, 0).endVertex();
						tess.pos(18, 18, 0).endVertex();
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
					} else if (t <= 1) {
						tess.pos(0, 18, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
						tess.pos(9, 0, 0).endVertex();
						tess.pos(0, 0, 0).endVertex();

						tess.pos(0, 18, 0).endVertex();
						tess.pos(18, 18, 0).endVertex();
						tess.pos(18, 0, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();

						tess.pos(18, 0, 0).endVertex();
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(dx, dy, 0).endVertex();
						tess.pos(9, 9, 0).endVertex();
					} else {
						tess.pos(0, 18, 0).endVertex();
						tess.pos(18, 18, 0).endVertex();
						tess.pos(18, 0, 0).endVertex();
						tess.pos(0, 0, 0).endVertex();
					}

					instance.draw();


					GlStateManager.enableTexture2D();
					GlStateManager.enableAlpha();
					GlStateManager.disableBlend();
				}
				GlStateManager.popMatrix();
				states.restore();

			}

			@Override
			@SideOnly(Side.CLIENT)
			public List<String> getToolTip() {
				ItemRef ref = getRef();
				if (ref == ItemRef.NULL) return null;
				ItemStack itemStack = ref.createItemStack(1);
				if (StackHelper.isNull(itemStack)) return null;
				return CompatHelper112.getTooltip(itemStack, player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
			}



			@Override
			@SideOnly(Side.CLIENT)
			public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				gui.drawSlotBackground(guiLeft + getX(), guiTop + getY());
			}

			@Override
			@SideOnly(Side.CLIENT)
			public Object getJEIIngredient() {
				ItemRef ref = getRef();
				return ref.createItemStack(1);
			}
		}
	}
}
