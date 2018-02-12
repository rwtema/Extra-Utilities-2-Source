package com.rwtema.extrautils2.tile;

import com.google.common.collect.HashMultimap;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.CraftingHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.NullRecipe;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.*;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ArrayAccess;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TileAnalogCrafter extends TilePower implements ITickable, IDynamicHandler {
	private static final byte NO_FACE = -1;
	private static final byte ANY_FACE = 7;
	private final static int TICK_TIME = 4;
	private final SingleStackHandler output = registerNBT("output", new SingleStackHandler() {
		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	});
	private final StackDump extraStacks = registerNBT("extrastacks", new StackDump());
	private final SingleStackHandlerFilter.ItemFilter[] filters;
	private final NBTSerializable.Int progress = registerNBT("progress", new NBTSerializable.Int());
	private final NBTSerializable.Int max_progress = registerNBT("max_progress", new NBTSerializable.Int(-1));
	private final NBTSerializable.NBTBoolean sticky = registerNBT("sticky", new NBTSerializable.NBTBoolean(false));
	private final NBTSerializable.NBTBoolean spread = registerNBT("spread", new NBTSerializable.NBTBoolean(false));
	public NBTSerializable.NBTEnum<RedstoneState> redstone_state = registerNBT("redstone", new NBTSerializable.NBTEnum<>(RedstoneState.OPERATE_ALWAYS));
	public NBTSerializable.NBTBoolean powered = registerNBT("powered", new NBTSerializable.NBTBoolean());
	public NBTSerializable.Int pulses = registerNBT("pulses", new NBTSerializable.Int());
	NBTSerializable.NBTByteArray slot_sides = registerNBT("slot_sides", new NBTSerializable.NBTByteArray(
			new byte[]{
					ANY_FACE, ANY_FACE, ANY_FACE,
					ANY_FACE, ANY_FACE, ANY_FACE,
					ANY_FACE, ANY_FACE, ANY_FACE,
			}));
	XUCrafter crafter = new XUCrafter();
	IRecipe curRecipe;
	private final ItemStackHandler contents = registerNBT("contents", new XUTileItemStackHandler(9, this) {


		@Override
		protected int getStackLimit(int slot, @ItemStackNonNull ItemStack stack) {
			if (!filters[slot].matches(stack) || slot_sides.array[slot] == NO_FACE) {
				return 0;
			}
			return super.getStackLimit(slot, stack);
		}

		@Override
		public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
			super.setStackInSlot(slot, stack);
			curRecipe = null;
		}

		@ItemStackNonNull
		@Override
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			if (!simulate && StackHelper.isNonNull(stack) && StackHelper.isNull(getStackInSlot(slot))) {
				curRecipe = null;
			}
			return super.insertItem(slot, stack, simulate);
		}

		@ItemStackNonNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack stack = super.extractItem(slot, amount, simulate);
			if (!simulate && StackHelper.isNonNull(stack) && StackHelper.isNull(getStackInSlot(slot))) {
				curRecipe = null;
			}

			return stack;
		}
	});
	private IItemHandler[] sideHandlers;
	private PublicWrapper.Extract extractHandler = new PublicWrapper.Extract(output);

	{
		filters = new SingleStackHandlerFilter.ItemFilter[9];
		for (int i = 0; i < 9; i++) {
			filters[i] = new SingleStackHandlerFilter.ItemFilter() {
				@Override
				protected void onContentsChanged() {
					markDirty();
				}
			};
		}
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		if (facing == null) return contents;
		return getSideHandler(facing.ordinal());
	}

	public IItemHandler getSideHandler(int face) {
		IItemHandler[] sideHandlers = this.sideHandlers;
		if (sideHandlers == null) {
			this.sideHandlers = sideHandlers = new IItemHandlerCompat[6];
		}

		IItemHandler sideHandler = sideHandlers[face];
		if (sideHandler == null) {
			TIntHashSet slots = new TIntHashSet();

			for (int i = 0; i < 9; i++) {
				byte side = slot_sides.array[i];
				if (side == face || side == ANY_FACE) {
					slots.add(i);
				}
			}


			if (slots.isEmpty()) {
				sideHandlers[face] = extractHandler;
			} else {
				int[] slotArray = slots.toArray();
				sideHandler = ConcatItemHandler.concatNonNull(
						new IItemHandlerModifiableCompat() {
							@Override
							public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
								contents.setStackInSlot(slotArray[slot], stack);
							}

							@Override
							public int getSlots() {
								return slotArray.length;
							}

							@ItemStackNonNull
							@Override
							public ItemStack getStackInSlot(int slot) {
								return contents.getStackInSlot(slotArray[slot]);
							}

							@ItemStackNonNull
							@Override
							public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
								return contents.insertItem(slotArray[slot], stack, simulate);
							}

							@ItemStackNonNull
							@Override
							public ItemStack extractItem(int slot, int amount, boolean simulate) {
								return StackHelper.empty();
							}
						},
						extractHandler
				);
			}
			sideHandlers[face] = sideHandler;
		}

		return sideHandler;
	}

	private IRecipe getRecipe() {
		if (curRecipe != null)
			return curRecipe;

		crafter.loadStacks(contents);
		List<IRecipe> recipes = CraftingHelper112.getRecipeList();
		for (IRecipe recipe : recipes) {
			try {
				if (recipe.matches(crafter, world)) {
					curRecipe = recipe;
					return recipe;
				}
			} catch (Exception err) {
				throw new RuntimeException("Caught exception while querying recipe " + TileCrafter.errLog(recipe), err);
			}
		}
		if (curRecipe == null) {
			curRecipe = NullRecipe.INSTANCE;
		}
		return curRecipe;
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public void update() {
		if (world.isRemote) return;

		if (extraStacks.hasStacks()) {
			extraStacks.attemptDump(output);
		}

		if ((world.getTotalWorldTime() % TICK_TIME) != 0) {
			return;
		}

		IRecipe recipe = getRecipe();
		if (recipe == NullRecipe.INSTANCE) {
			progress.value = 0;
			max_progress.value = -1;
			return;
		}

		if (spread.value) {
			trySpreadItems();
		}

		switch (redstone_state.value) {
			case OPERATE_REDSTONE_ON:
				if (!powered.value) {
					progress.value = max_progress.value = 0;
					return;
				}
				break;
			case OPERATE_REDSTONE_OFF:
				if (powered.value) {
					progress.value = max_progress.value = 0;
					return;
				}
				break;
			case OPERATE_REDSTONE_PULSE:
				if (pulses.value == 0) {
					progress.value = max_progress.value = 0;
					return;
				}
				break;
		}

		if (sticky.value) {
			for (int i = 0; i < contents.getSlots(); i++) {
				ItemStack stackInSlot = contents.getStackInSlot(i);
				if (StackHelper.isNonNull(stackInSlot)) {
					if (StackHelper.getStacksize(stackInSlot) == 1 && stackInSlot.getMaxStackSize() > 1) {
						progress.value = 0;
						max_progress.value = 0;
						return;
					}
				}
			}
		}

		crafter.loadStacks(contents);

		if (!recipe.matches(crafter, world)) {
			return;
		}

		ItemStack craftingResult = recipe.getCraftingResult(crafter);
		if (StackHelper.isNull(craftingResult) || StackHelper.isNonNull(output.insertItem(0, craftingResult, true))) {
			max_progress.value = 0;
			progress.value = 0;
			return;
		}

		if (max_progress.value <= 0) {
			max_progress.value = StackHelper.getStacksize(craftingResult) * TICK_TIME * 5;
		}

		progress.value += TICK_TIME;

		if (progress.value >= max_progress.value) {
			if (pulses.value > 0)
				pulses.value--;

			progress.value = 0;
			output.insertItem(0, craftingResult, false);

			ArrayAccess<ItemStack> remainingStacks = CompatHelper.getArray10List11(CompatHelper.getRemainingItems(crafter, world));
			for (int i = 0; i < remainingStacks.length(); ++i) {
				ItemStack curStack = crafter.getStackInSlot(i);
				ItemStack remainStack = remainingStacks.get(i);

				if (StackHelper.isNonNull(curStack)) {
					contents.extractItem(i, 1, false);
				}

				if (StackHelper.isNonNull(remainStack)) {
					remainStack = contents.insertItem(i, remainStack, false);
					if (StackHelper.isNonNull(remainStack)) {
						extraStacks.addStack(remainStack);
					}
				}
			}

		}
	}

	private void trySpreadItems() {
		HashMultimap<ItemRef, Integer> slotMap = HashMultimap.create();
		for (int i = 0; i < 9; i++) {
			ItemStack stackInSlot = contents.getStackInSlot(i);
			if (StackHelper.isNull(stackInSlot)) continue;

			if (stackInSlot.getMaxStackSize() == 1) continue;

			ItemRef itemRef = ItemRef.wrap(stackInSlot);
			if (itemRef != ItemRef.NULL)
				slotMap.put(itemRef, i);
		}

		if (slotMap.isEmpty()) return;

		for (ItemRef ref : slotMap.keySet()) {
			Set<Integer> set = slotMap.get(ref);
			if (set.size() <= 1) continue;

			int biggestSlot = -1, biggestSize = -1;
			int smallestSlot = -1, smallestSize = Integer.MAX_VALUE;
			for (Integer slot : set) {
				ItemStack stackInSlot = contents.getStackInSlot(slot);
				if (StackHelper.isNull(stackInSlot)) continue;
				int stacksize = StackHelper.getStacksize(stackInSlot);
				if (biggestSize < stacksize) {
					biggestSize = stacksize;
					biggestSlot = slot;
				}
				if (smallestSize > stacksize) {
					smallestSize = stacksize;
					smallestSlot = slot;
				}
			}
			if (smallestSlot == biggestSlot) continue;
			int t = biggestSize - smallestSize;
			if (t >= 2) {
				ItemStack smallestStack = contents.getStackInSlot(smallestSlot);
				ItemStack biggestStack = contents.getStackInSlot(biggestSlot);
				if (ItemHandlerHelper.canItemStacksStack(smallestStack, biggestStack)) {
					StackHelper.increase(smallestStack);
					StackHelper.decrease(biggestStack);
					markDirty();
				}
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		boolean wasPowered = powered.value;
		boolean newPower = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
		if (newPower != wasPowered) {
			powered.value = newPower;
			if (newPower && redstone_state.value == RedstoneState.OPERATE_REDSTONE_PULSE) {
				pulses.value++;
			}
		}

	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerAnalogCrafter(this, player);
	}

	public static class ContainerAnalogCrafter extends DynamicContainerTile {
		static final TByteObjectHashMap<float[]> side_colors = new TByteObjectHashMap<>();

		static {
			side_colors.put(NO_FACE, new float[]{0.7F, 0.7F, 0.7F});
			side_colors.put(ANY_FACE, new float[]{1, 1, 1});
			side_colors.put((byte) 0, new float[]{1.0F, 0.7F, 1.0F});
			side_colors.put((byte) 1, new float[]{0.7F, 1.0F, 1.0F});
			side_colors.put((byte) 2, new float[]{1.0F, 0.7F, 0.7F});
			side_colors.put((byte) 3, new float[]{0.7F, 0.7F, 1.0F});
			side_colors.put((byte) 4, new float[]{0.7F, 1.0F, 0.7F});
			side_colors.put((byte) 5, new float[]{1.0F, 1.0F, 0.7F});
		}

		public ContainerAnalogCrafter(TileAnalogCrafter tile, EntityPlayer player) {
			super(tile);

			addTitle(Lang.getItemName(XU2Entries.analogCrafter.value));

			crop();

			int u = height;

//			int l = playerInvWidth;
//			int l2 = 8 + (l - (3 * 18 + 4 * 18 + 8 + WidgetProgressArrowNetworkBase.ARROW_WIDTH)) / 2;
			int l2 = 4;

			addWidget(new WidgetProgressArrowTimer(l2 + 3 * 18 + 4, u + 18) {
				@Override
				protected float getTime() {
					return tile.progress.value;
				}

				@Override
				protected float getMaxTime() {
					return tile.max_progress.value;
				}
			});

//			DynamicWindow subWindow = new DynamicWindow(DynamicWindow.WindowSide.LEFT);


			addWidget(new WidgetSlotItemHandler(tile.extractHandler, 0, l2 + 3 * 18 + WidgetProgressArrowNetworkBase.ARROW_WIDTH + 8, u + 18));

			addWidget(new WidgetRawData() {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeBytes(tile.slot_sides.array);
				}

				@Override
				public void handleDescriptionPacket(XUPacketBuffer packet) {
					packet.data.readBytes(tile.slot_sides.array);
				}
			});

			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					int i = x + y * 3;
					addWidget(new WidgetSlotItemHandler(tile.contents, i, l2 + x * 18, u + y * 18) {
						@Override
						public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
							float[] cols = side_colors.get(tile.slot_sides.array[i]);
							if (cols != null) GlStateManager.color(cols[0], cols[1], cols[2], 1);
							super.renderBackground(manager, gui, guiLeft, guiTop);
							if (cols != null) GlStateManager.color(1, 1, 1, 1);
						}
					});


					WidgetClickMCButtonChoices<Byte> button = new WidgetClickMCButtonChoices<Byte>(l2 + 8 + 3 * 18 + WidgetProgressArrowNetworkBase.ARROW_WIDTH + 8 + 18 + 18 * x, u + y * 18) {
						@Override
						protected void onSelectedServer(Byte marker) {
							tile.slot_sides.array[i] = marker;
						}

						@Override
						public Byte getSelectedValue() {
							return tile.slot_sides.array[i];
						}
					};

					button.addChoice(NO_FACE, "x", Lang.translate("Disabled"));
					for (EnumFacing facing : EnumFacing.values()) {
						button.addChoice(((byte) facing.ordinal()), facing.getName2().substring(0, 1).toLowerCase(Locale.ENGLISH), Lang.translate(StringHelper.capFirst(facing.getName2()) + " Only"));
					}
					button.addChoice(ANY_FACE, "a", Lang.translate("Accessible from all Sides"));
					addWidget(button);
				}
			}
			crop();

			IWidget w;
			addWidget(w = new WidgetClickMCButtonBoolean.NBTBoolean(4, height + 4,
					tile.sticky,
					Lang.translate("Sticky"),
					Lang.translate("Keep at least one item in each slot (does not apply to items that do not stack)"))
			);

			addWidget(new WidgetClickMCButtonBoolean.NBTBoolean(w.getX() + w.getW() + 4, height + 4,
					tile.spread,
					Lang.translate("Spread Items"),
					Lang.translate("Distribute input items evenly among existing stacks"))
			);

			crop();

			addWidget(TileAdvInteractor.getRSWidget(4, height + 4, tile.redstone_state, tile.pulses));

			cropAndAddPlayerSlots(player.inventory);

			validate();
		}
	}
}
