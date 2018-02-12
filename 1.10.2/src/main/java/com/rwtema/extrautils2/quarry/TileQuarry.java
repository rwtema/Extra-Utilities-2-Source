package com.rwtema.extrautils2.quarry;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.dimensions.workhousedim.WorldProviderSpecialDim;
import com.rwtema.extrautils2.eventhandlers.ItemCaptureHandler;
import com.rwtema.extrautils2.eventhandlers.XPCaptureHandler;
import com.rwtema.extrautils2.fakeplayer.XUFakePlayer;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.itemhandler.StackDump;
import com.rwtema.extrautils2.items.ItemBiomeMarker;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.tile.RedstoneState;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ListRandomOffset;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class TileQuarry extends TilePower implements ITickable, IDynamicHandler {

	static final ArrayList<BlockPos> offset = new ArrayList<>();
	static final HashMap<BlockPos, ArrayList<EnumFacing>> offset_sides = new HashMap<>();
	private final static ItemStack genericDigger = new ItemStack(Items.DIAMOND_PICKAXE, 1);
	public static int ENERGY_PER_OPERATION = 16000;

	static {
		BlockPos origin = BlockPos.ORIGIN;
		for (EnumFacing facing1 : EnumFacing.values()) {
			BlockPos offset1 = origin.offset(facing1);
			for (EnumFacing facing2 : EnumFacing.values()) {
				if (facing1 != facing2.getOpposite()) {
					BlockPos offset2 = offset1.offset(facing2);
					if (!offset.contains(offset2))
						offset.add(offset2);
					offset_sides.computeIfAbsent(offset2, t -> new ArrayList<>()).add(facing2.getOpposite());
				}
			}
		}
	}

	private final StackDump extraStacks = registerNBT("extrastacks", new StackDump());
	public boolean redstoneDirty = true;
	public boolean redstoneActive;
	protected SingleStackHandlerFilter.ItemFilter filter = registerNBT("filter", new SingleStackHandlerFilter.ItemFilter() {
		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	});
	Biome lastBiome = null;
	XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(ENERGY_PER_OPERATION * 10));
	ChunkPos posKey = null;
	ChunkPos chunkPos = null;
	NBTSerializable.Int curBlockLocation = registerNBT("location", new NBTSerializable.Int(0));
	long last_tick_time = -1;
	int num_ticks = 0;
	NBTSerializable.Long blocksMined = registerNBT("mined", new NBTSerializable.Long());
	BlockPos.MutableBlockPos digPos = new BlockPos.MutableBlockPos();
	boolean needsToCheckNearbyBlocks = true;
	boolean hasNearbyBlocks = false;
	private ItemBiomeMarker.ItemBiomeHandler biomeHandler = registerNBT("biome_marker", new ItemBiomeMarker.ItemBiomeHandler() {
		@Override
		protected void onContentsChanged() {
			Biome biome = getBiome();
			if (biome != null && lastBiome != null && biome != lastBiome && chunkPos != null && posKey != null) {
				lastBiome = biome;
				getNewChunk();
			}
			markDirty();
		}
	});

	private NBTSerializable.NBTEnum<RedstoneState> redstone_state = registerNBT("redstone_state", new NBTSerializable.NBTEnum<>(RedstoneState.OPERATE_ALWAYS));
	private ItemStack diggerTool;
	private final SingleStackHandler enchants = registerNBT("enchants", new SingleStackHandler() {
		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			if (stack.getItem() != Items.ENCHANTED_BOOK) {
				return 0;
			}

			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
			if (map.isEmpty())
				return 0;
			for (Enchantment enchantment : map.keySet()) {
				if (enchantment.canApply(genericDigger))
					return 1;
			}

			return 0;
		}

		@Override
		protected void onContentsChanged() {
			markDirty();
			diggerTool = null;
		}
	});
	private XUFakePlayer fakePlayer;

	{
		registerNBT("pos_key_2", new INBTSerializable<NBTTagCompound>() {

			@Override
			public NBTTagCompound serializeNBT() {
				NBTTagCompound tags = new NBTTagCompound();
				if (posKey != null && chunkPos != null) {
					tags.setInteger("x", posKey.x);
					tags.setInteger("z", posKey.z);
				}
				return tags;
			}

			@Override
			public void deserializeNBT(NBTTagCompound nbt) {
				if (nbt.hasNoTags()) {
					posKey = null;
					chunkPos = null;
				} else {
					posKey = new ChunkPos(nbt.getInteger("x"), nbt.getInteger("z"));
					chunkPos = WorldProviderSpecialDim.adjustChunkRef(posKey);
				}
			}
		});
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(
				InventoryHelper.getItemHandlerIterator(filter),
				InventoryHelper.getItemHandlerIterator(enchants),
				InventoryHelper.getItemHandlerIterator(biomeHandler)
		);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		if (posKey != null)
			WorldProviderSpecialDim.releaseChunk(posKey);
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void onPowerChanged() {

	}

	public boolean hasNearbyBlocks() {
		if (needsToCheckNearbyBlocks || world.isRemote) {
			needsToCheckNearbyBlocks = false;
			hasNearbyBlocks = true;
			for (EnumFacing facing : EnumFacing.values()) {
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));

				if (!(tileEntity instanceof TileQuarryProxy) || ((TileQuarryProxy) tileEntity).facing.value != facing.getOpposite()) {
					hasNearbyBlocks = false;
					break;
				}
			}
		}
		return hasNearbyBlocks;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return hasNearbyBlocks() && super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}

	@Override
	public void update() {
		if (world.isRemote) {
			return;
		}

		if (!hasNearbyBlocks()) return;

		if (redstoneDirty) {
			redstoneDirty = false;
			redstoneActive = false;

			for (EnumFacing facing : EnumFacing.values()) {
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));

				if ((tileEntity instanceof TileQuarryProxy) && ((TileQuarryProxy) tileEntity).facing.value == facing.getOpposite() && ((TileQuarryProxy) tileEntity).isPowered()) {
					redstoneActive = true;
					break;
				}
			}
		}

		if (!redstone_state.value.acceptableValue(redstoneActive)) return;

		long time = world.getTotalWorldTime();
		if (last_tick_time == -1 || time != last_tick_time) {
			last_tick_time = time;
			if (num_ticks > 0) {
				if (world.rand.nextInt(20) == 0) {
					num_ticks--;
				} else {
					for (EnumFacing enumFacing : EnumFacing.values()) {
						if (world.rand.nextInt(20 * 10) < num_ticks) {
							world.playEvent(2004, pos.offset(enumFacing), 0);
						}
					}

					if (num_ticks > (20 * 10)) {
						world.createExplosion(null,
								pos.getX() + 0.5,
								pos.getY() + 0.5,
								pos.getZ() + 0.5,
								6, true
						);
					}
				}
			}

		} else {
			num_ticks++;
		}

		int numOps = 1;
		ItemStack enchantsStack = enchants.getStack();
		if (StackHelper.isNonNull(enchantsStack)) {
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantsStack);
			Integer integer = enchantments.get(Enchantments.EFFICIENCY);
			if (integer != null) {
				numOps += integer;
			}
		}


		for (int rep_i = 0; rep_i < numOps; rep_i++) {


			if (extraStacks.stacks.isEmpty() && energy.extractEnergy(ENERGY_PER_OPERATION, true) == ENERGY_PER_OPERATION) {
				WorldServer miningWorld = WorldProviderSpecialDim.getWorld();

				if (posKey == null) {
					getNextChunk();
				}


				if (fakePlayer == null) {
					fakePlayer = new XUFakePlayer(miningWorld, owner, Lang.getItemName(getXUBlock()));
				}

				if (StackHelper.isNull(diggerTool)) {
					diggerTool = genericDigger.copy();
					if (StackHelper.isNonNull(enchantsStack)) {
						EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(enchantsStack), diggerTool);
					}
				}

				IBlockState state;

				while (true) {
					setBlockPos();
					state = miningWorld.getBlockState(digPos);
					if (state.getBlock().isAir(state, miningWorld, digPos)) {
						advance();
					} else {
						float hardness = state.getMaterial().isLiquid() ? 0 : state.getPlayerRelativeBlockHardness(fakePlayer, miningWorld, digPos);
						if (hardness > 0) {
							break;
						} else
							advance();
					}
				}

				lastBiome = miningWorld.getBiomeForCoordsBody(digPos);

				float hardness = state.getMaterial().isLiquid() ? 0 : state.getPlayerRelativeBlockHardness(fakePlayer, miningWorld, digPos);
				if (hardness > 0) {
					fakePlayer.setHeldItem(EnumHand.MAIN_HAND, diggerTool.copy());
					fakePlayer.setLocationEdge(digPos, EnumFacing.DOWN);

					ItemCaptureHandler.startCapturing();
					XPCaptureHandler.startCapturing();
					try {
						fakePlayer.interactionManager.tryHarvestBlock(digPos);
					} catch (Throwable err) {
						XPCaptureHandler.stopCapturing();
						ItemCaptureHandler.stopCapturing();
						throw Throwables.propagate(err);
					}

					blocksMined.value++;

					XPCaptureHandler.stopCapturing();
					LinkedList<ItemStack> stacks = ItemCaptureHandler.stopCapturing();

					for (ItemStack stack : stacks) {
						if (filter.matches(stack)) {
							extraStacks.addStack(stack);
						}
					}

					fakePlayer.setHeldItem(EnumHand.MAIN_HAND, StackHelper.empty());

					InventoryPlayer inventory = fakePlayer.inventory;
					for (int i = 0; i < inventory.getSizeInventory(); i++) {
						ItemStack stack = inventory.getStackInSlot(i);
						if (StackHelper.isNonNull(stack) && StackHelper.getStacksize(stack) > 0) {
							if (filter.matches(stack)) {
								extraStacks.addStack(stack);
							}
						}
					}

					fakePlayer.clearInventory();

					energy.extractEnergy(ENERGY_PER_OPERATION, false);
				}
				advance();
			}

			if (!extraStacks.stacks.isEmpty()) {
				for (BlockPos offset_pos : new ListRandomOffset<>(offset)) {
					TileEntity tile = world.getTileEntity(pos.add(offset_pos));
					if (tile != null) {
						for (EnumFacing facing : new ListRandomOffset<>(offset_sides.get(offset_pos))) {
							IItemHandler handler = CapGetter.ItemHandler.getInterface(tile, facing);
							if (handler != null) {
								extraStacks.attemptDump(handler);
							}
						}
					}
				}
			}
		}

	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
		needsToCheckNearbyBlocks = true;
	}

	private void setBlockPos() {
		int value = curBlockLocation.value;
		int y = getY(value);
		int x = getX(value);
		int z = getZ(value);

		digPos.setPos((chunkPos.x << 4) + x, y, (chunkPos.z << 4) + z);
	}

	private int getZ(int value) {
		return (value >> 12) & 15;
	}

	private int getX(int value) {
		return (value >> 8) & 15;
	}

	private void advance() {
		int value = curBlockLocation.value;
		curBlockLocation.value++;

		int y = getY(value);
		if (y > 0) return;
		int x = getX(value);
		int z = getZ(value);


		if (y < 0 || (y == 0 && x == 15 && z == 15)) {
			getNewChunk();
		}
	}

	private void getNewChunk() {
		WorldProviderSpecialDim.releaseChunk(posKey);

		curBlockLocation.value = 0;
		posKey = null;
		chunkPos = null;

		getNextChunk();
	}

	private int getY(int value) {
		return 255 - (value & 255);
	}

	private void getNextChunk() {
		posKey = WorldProviderSpecialDim.prepareNewChunk(biomeHandler.getBiome());
		chunkPos = WorldProviderSpecialDim.adjustChunkRef(posKey);
		curBlockLocation.value = 0;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerQuarry(player);
	}

	public class ContainerQuarry extends DynamicContainerTile {

		public ContainerQuarry(EntityPlayer player) {
			super(TileQuarry.this);
			addTitle("Quantum Quarry");
			addWidget(new SingleStackHandlerFilter.WidgetSlotFilter(filter, 4, 20) {
				@Override
				@SideOnly(Side.CLIENT)
				public List<String> getToolTip() {
					if (!getHasStack()) {
						return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(Lang.translate("If present, the quarry will auto-destroy any items that do NOT match the filter."), 120);
					}
					return null;
				}
			});
			addWidget(new WidgetSlotItemHandler(enchants, 0, 4, 20 + 18) {
				@Override
				@SideOnly(Side.CLIENT)
				public List<String> getToolTip() {
					if (!getHasStack()) {
						return ImmutableList.of(Lang.translate("Enchanted Book"));
					}
					return null;
				}

				@Override
				@SideOnly(Side.CLIENT)
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					super.renderBackground(manager, gui, guiLeft, guiTop);
					if (!getHasStack()) {
						ItemStack stack = ItemIngredients.Type.ENCHANTED_BOOK_SKELETON.newStack();
						gui.renderStack(stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
					}
				}
			});

			addWidget(biomeHandler.getSlot(4, 20 + 18 * 2));


			addWidget(new WidgetEnergyStorage(DynamicContainer.playerInvWidth - 18 - 4, 20, energy));

			addWidget(RedstoneState.getRSWidget(4, 20 + 37 + 18, redstone_state));

			crop();

			addWidget(new WidgetTextData(4 + 18 + 4, 20, (DynamicContainer.playerInvWidth - 18 * 2 - 16), 54, 1, 4210752) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeInt(TileQuarry.this.getY(curBlockLocation.value));
					packet.writeLong(TileQuarry.this.blocksMined.value);
					packet.writeInt(lastBiome != null ? Biome.REGISTRY.getIDForObject(lastBiome) : -1);
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					int y = packet.readInt();
					long mined = packet.readLong();
					int biomeID = packet.readInt();
					Biome biome = biomeID != -1 ? Biome.REGISTRY.getObjectById(biomeID) : null;

					return Lang.translateArgs("Quarry Level: %s", y) + "\n" + Lang.translateArgs("Blocks Mined: %s", mined)
							+ ((biome != null) ? ("\n" + Lang.translateArgs("Biome: %s", biome.getBiomeName())) : "");
				}
			});


			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
