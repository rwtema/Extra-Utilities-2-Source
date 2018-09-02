package com.rwtema.extrautils2.tile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.NBTSerializableRegisteredValue;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class TileScanner extends XUTile implements ITickable, IDynamicHandler {
	private final static LoadingCache<Block, Set<IBlockState>> associatedStates = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(new CacheLoader<Block, Set<IBlockState>>() {
		@Override
		public Set<IBlockState> load(@Nonnull Block key) throws Exception {
			HashSet<IBlockState> states = new HashSet<>();
			states.add(key.getDefaultState());
			BlockStateContainer blockState = key.getBlockState();
			states.addAll(blockState.getValidStates());
			Collection<IProperty<?>> properties = blockState.getProperties();
			int v;
			do {
				v = states.size();
				for (IProperty property : properties) {
					Collection<Comparable> values = property.getAllowedValues();
					for (Comparable value : values) {
						ArrayList<IBlockState> list = Lists.newArrayList();
						for (IBlockState state : states) {
							list.add(state.withProperty(property, value));
						}
						states.addAll(list);
					}
				}
			} while (states.size() != v);
			return states;
		}
	});
	private final static LoadingCache<Block, Set<Block>> associatedBlocks = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build(new CacheLoader<Block, Set<Block>>() {
		@Override
		public Set<Block> load(@Nonnull Block key) throws Exception {
			HashSet<Block> blocks = new HashSet<>();
			blocks.add(key);

			for (IBlockState state : associatedStates.getUnchecked(key)) {
				blocks.add(state.getBlock());
			}

			return blocks;
		}

	});
	NBTSerializableRegisteredValue<Block> storedBlock = registerNBT("block", new NBTSerializableRegisteredValue<>(ForgeRegistries.BLOCKS));
	NBTSerializable.HashMapSerializable<String, HashSet<String>, NBTTagList> storedPropertyBlacklist = registerNBT("props", new NBTSerializable.HashMapSerializable<>(
			UnaryOperator.identity(),
			UnaryOperator.identity(),
			new Function<HashSet<String>, NBTTagList>() {
				@Override
				public NBTTagList apply(HashSet<String> v) {
					NBTTagList list = new NBTTagList();
					for (String s : v) {
						if (s != null)
							list.appendTag(new NBTTagString(s));
					}
					return list;
				}
			},
			new Function<NBTTagList, HashSet<String>>() {
				@Override
				public HashSet<String> apply(NBTTagList list) {
					HashSet<String> strings = new HashSet<>();
					for (int i = 0; i < list.tagCount(); i++) {
						strings.add(list.getStringTagAt(i));
					}

					return strings;
				}
			}
	));

	NBTSerializable.NBTBoolean redstone = registerNBT("powered", new NBTSerializable.NBTBoolean());

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerScanner(this);
	}

	@Override
	public void update() {
		if (world.isRemote) return;
		boolean b = calcCurrentState();
		if (b != redstone.value) {
			redstone.value = b;
			CompatHelper.notifyNeighborsOfStateChange(world, getPos(), getXUBlock());
		}
	}

	public boolean calcCurrentState() {
		IBlockState state = getScannedBlockState();
		return checkState(state);
	}

	@Nonnull
	public IBlockState getScannedBlockState() {
		EnumFacing side = getBlockState().getValue(XUBlockStateCreator.ROTATION_ALL);
		BlockPos offset = getPos().offset(side);
		TileEntity tileEntity = world.getTileEntity(offset);
		World world = getWorld();
		if(tileEntity instanceof IRemoteTarget){
			Optional<Pair<World, BlockPos>> targetPos = ((IRemoteTarget) tileEntity).getTargetPos();
			if(targetPos.isPresent()){
				Pair<World, BlockPos> pair = targetPos.get();
				world = pair.getLeft();
				offset = pair.getRight();
			}
		}

		IBlockState state = world.getBlockState(offset);
		return state.getActualState(world, offset);
	}

	public boolean checkState(IBlockState state) {
		return matchBlock(state);
	}

	public boolean matchBlock(IBlockState state) {
		Block storedBlock = this.storedBlock.value;

		if (storedBlock == null) return false;
		Block block = state.getBlock();
		if (!associatedBlocks.getUnchecked(block).contains(storedBlock))
			return false;

		List<IProperty<?>> properties = Lists.newArrayList(storedBlock.getBlockState().getProperties());

		for (IProperty property : properties) {
			HashSet<String> set = storedPropertyBlacklist.map.get(property.getName());
			if (set == null) continue;
			String name = property.getName(state.getValue(property));
			if (set.contains(name))
				return false;
		}

		return true;
	}

	public boolean isPowered() {
		return redstone.value;
	}

	class ContainerScanner extends DynamicContainerTile {
		public static final int BUTTON_HEIGHT = 20;
		public static final int CACHE = 16 * 8;
		public static final int NUM_ROWS = 5;
		public static final int BUTTONS_PER_ROW = 4;
		public static final int BUTTON_WIDTH = 320 / BUTTONS_PER_ROW;
		IWidget[][] values;
		MyWidgetClickMCButtonText[] buttonCache = new MyWidgetClickMCButtonText[CACHE];
		PropertyText[] textCache = new PropertyText[CACHE];
		WidgetScrollBar scrollBar;

		HashMap<String, String> currentValues = new HashMap<>();

		int scrollValue = 0;

		public ContainerScanner(TileScanner tile) {
			super(tile);

			addTitle("Block Detector");
			int w = BUTTON_WIDTH * BUTTONS_PER_ROW - 120 - 12 + 8 + WidgetScrollBar.BAR_WIDTH;
			addWidget(new WidgetTextData(4, 6 + 9 + (20 - 9) / 2, w, 9) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					Block value = storedBlock.value;
					if (value == null) {
						packet.writeInt(-1);
					} else
						packet.writeInt(Block.getIdFromBlock(value));

					packet.writeNBT(storedPropertyBlacklist.serializeNBT());

					IBlockState state = getScannedBlockState();
					ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
					packet.writeInt(properties.size());
					for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
						packet.writeString(entry.getKey().getName());
						packet.writeString(((IProperty) entry.getKey()).getName(entry.getValue()));
					}
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					int id = packet.readInt();
					Block block = id != -1 ? Block.getBlockById(id) : null;
					storedBlock.value = block;
					storedPropertyBlacklist.deserializeNBT(packet.readNBT());
					int n = packet.readInt();
					currentValues.clear();
					for (int i = 0; i < n; i++) {
						currentValues.put(packet.readString(), packet.readString());
					}
					assignValues();
					if (block == Blocks.AIR)
						return Lang.translate("Current Block: ") + I18n.format("createWorld.customize.flat.air");
					else if (block != null) return Lang.translate("Current Block: ") + block.getLocalizedName();
					else return Lang.translate("Current Block: ") + Lang.translate("Unassigned");
				}
			});

			addWidget(new WidgetClickMCButtonText(Lang.translate("Set to Current Block"), w + 12, 6 + 9 + (20 - 9) / 2, 120, 20) {
				@Nullable
				@Override
				public XUPacketBuffer getPacketToSend(int mouseButton) {
					return new XUPacketBuffer();
				}

				@Override
				public void receiveClientPacket(XUPacketBuffer buffer) {
					IBlockState scan = getScannedBlockState();
					storedBlock.value = scan.getBlock();
					storedPropertyBlacklist.map.clear();
					markDirty();
				}
			});

			addWidget(new WidgetText(4, 48, Lang.translate("Select which properties are allowed")));
			final int y = 48 + 8;
			addWidget(scrollBar = new WidgetScrollBar(BUTTON_WIDTH * BUTTONS_PER_ROW + 8, y, NUM_ROWS * BUTTON_HEIGHT, 0, 0) {
				@Override
				protected void onChange() {
					for (MyWidgetClickMCButtonText button : buttonCache) {
						button.hide();
					}
					for (PropertyText propertyText : textCache) {
						propertyText.hide();
					}

					IWidget[][] values = ContainerScanner.this.values;
					if (values == null) {
						return;
					}

					for (int i = 0; i < NUM_ROWS; i++) {
						int j = i + scrollBar.scrollValue;
						if (j >= 0 && j < values.length) {
							IWidget[] rows = values[j];
							for (int k = 0; k < rows.length; k++) {
								IWidget w = rows[k];
								if (w instanceof MyWidgetClickMCButtonText) {
									((MyWidgetClickMCButtonText) w).show(5 + k * BUTTON_WIDTH, y + i * BUTTON_HEIGHT);
								} else if (w instanceof PropertyText) {
									((PropertyText) w).show(5, y + BUTTON_HEIGHT - 10 + i * BUTTON_HEIGHT);
								}
							}
						}
					}


				}
			});
			for (int i = 0; i < buttonCache.length; i++) {
				buttonCache[i] = new MyWidgetClickMCButtonText();
				addWidget(buttonCache[i]);
			}
			for (int i = 0; i < textCache.length; i++) {
				textCache[i] = new PropertyText();
				addWidget(textCache[i]);
			}

			addWidget(new WidgetText(4, scrollBar.getY() + scrollBar.getH() + 4, Lang.translate("[] signifies the current value of the property")));

			crop();

			validate();
		}

		private void assignValues() {
			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					for (MyWidgetClickMCButtonText text : buttonCache) {
						text.clear();
					}
					for (PropertyText text : textCache) {
						text.clear();
					}
					List<IWidget[]> wRows = new ArrayList<>();
					Block value = storedBlock.value;
					int i = 0;
					int j = 0;
					if (value != null) {
						List<IProperty<?>> properties = Lists.newArrayList(value.getBlockState().getProperties());
						properties.sort((o1, o2) -> ComparisonChain.start()
								.compare(o1.getValueClass().getSimpleName(), o2.getValueClass().getSimpleName())
								.compare(o1.getName(), o2.getName())
								.result());

						for (IProperty property : properties) {
							PropertyText propertyText = textCache[j];
							j++;
							propertyText.setText(StringHelper.capitalizeProp(property.getName()));
							wRows.add(new IWidget[]{propertyText});
							ArrayList<IWidget> w = new ArrayList<>();
							for (Object val : property.getAllowedValues()) {
								MyWidgetClickMCButtonText button = buttonCache[i];
								i++;
								button.show(property.getName(), property.getName((Comparable) val));
								w.add(button);
								if (w.size() == BUTTONS_PER_ROW) {
									wRows.add(w.toArray(new IWidget[0]));
									w = new ArrayList<>();
								}
							}
							if (!w.isEmpty()) {
								wRows.add(w.toArray(new IWidget[0]));
							}
						}
					}


					values = wRows.toArray(new IWidget[wRows.size()][]);
					scrollBar.setValues(0, Math.max(0, values.length - NUM_ROWS));
				}
			});
		}

		private class PropertyText extends WidgetText {

			boolean visible = false;

			public PropertyText() {
				super(0, 0, "", BUTTON_WIDTH * BUTTONS_PER_ROW);
			}

			@Override
			public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				if (visible)
					super.renderBackground(manager, gui, guiLeft, guiTop);
			}

			public void hide() {
				this.visible = false;
			}

			public void show(int x, int y) {
				this.x = x;
				this.y = y;
				this.visible = true;
			}

			public void setText(String text) {
				this.msg = text;
			}

			public void clear() {
				msg = "";
				visible = false;
				x = 0;
				y = 0;
			}
		}

		private class MyWidgetClickMCButtonText extends WidgetClickMCButtonText {
			String property;
			String value;
			boolean visible;

			public MyWidgetClickMCButtonText() {
				super("", 0, 0, ContainerScanner.BUTTON_WIDTH, ContainerScanner.BUTTON_HEIGHT);
			}

			@Nullable
			@Override
			public XUPacketBuffer getPacketToSend(int mouseButton) {
				if (property != null && value != null) {
					XUPacketBuffer buffer = new XUPacketBuffer();
					buffer.writeBoolean(DynamicGui.isShiftKeyDown());
					buffer.writeString(property);
					buffer.writeString(value);
					return buffer;
				}

				return null;
			}

			@Override
			public void receiveClientPacket(XUPacketBuffer buffer) {
				boolean shift = buffer.readBoolean();
				String prop = buffer.readString();
				String val = buffer.readString();
				IBlockState state = getScannedBlockState();
				Block block = state.getBlock();
				final Optional<IProperty<?>> any = block.getBlockState().getProperties().stream().filter(iProperty -> iProperty.getName().equals(prop)).findAny();
				if (any.isPresent()) {
					if (shift) {
						HashSet<String> set = new HashSet<>();
						IProperty property = any.get();
						for (Object comparable : property.getAllowedValues()) {
							String name = property.getName((Comparable) comparable);
							if (!val.equals(name)) {
								set.add(name);
							}
						}
						storedPropertyBlacklist.map.put(prop, set);
					} else {

						HashSet<String> set = storedPropertyBlacklist.map.get(prop);
						if (set == null) {
							set = new HashSet<>();
							set.add(val);
							storedPropertyBlacklist.map.put(prop, set);
						} else {
							if (set.contains(val)) {
								set.remove(val);
								if (set.isEmpty()) {
									storedPropertyBlacklist.map.remove(prop);
								}
							} else {
								set.add(val);
							}
						}
					}
				}
			}

			public void hide() {
				visible = false;
			}

			public void show(String property, String value) {
				this.property = property;
				this.value = value;
				mouseOver = false;
				visible = true;
				HashSet<String> set = storedPropertyBlacklist.map.get(property);
				String s = StringHelper.capitalizeProp(value);
				if (value.equals(currentValues.get(property))) {
					s = "[" + s + "]";
				}
				if (set != null && set.contains(value)) {
					text = ChatFormatting.STRIKETHROUGH + s + ChatFormatting.RESET;
				} else {
					text = s;
				}
			}

			@Override
			public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				if (visible && property != null && value != null)
					super.renderBackground(manager, gui, guiLeft, guiTop);
			}

			@Override
			public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				if (visible && property != null && value != null)
					super.renderForeground(manager, gui, guiLeft, guiTop);
			}

			public void show(int x, int y) {
				this.x = x;
				this.y = y;
				mouseOver = false;
				visible = true;
			}

			public void clear() {
				visible = false;
				property = null;
				value = null;

			}
		}
	}
}
