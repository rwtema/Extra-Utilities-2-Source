package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.IItemHandlerCompat;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TransferNodeItem extends TransferNodeBase<IItemHandler> implements IDynamicHandler {
	protected SingleStackHandlerFilter.ItemFilter filter = registerNBT("Filter", new SingleStackHandlerFilter.ItemFilter() {
		@Override
		protected void onContentsChanged() {
			holder.markDirty();
		}
	});

	protected SingleStackHandler stack = registerNBT("Buffer", new SingleStackHandler() {
		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			if (!filter.matches(stack)) return 0;
			return stack.getMaxStackSize();
		}

		@Override
		protected void onContentsChanged() {
			holder.markDirty();
		}
	});

	@Override
	public List<ItemStack> getDrops() {
		List<ItemStack> drops = new ArrayList<>();
		drops.add(getBaseDrop());
		if (!stack.isEmpty()) drops.add(stack.getStack());
		for (ItemStack itemStack : InventoryHelper.getItemHandlerIterator(upgradeHandler)) {
			if (StackHelper.isNonNull(itemStack)) {
				drops.add(itemStack);
			}
		}
		if (StackHelper.isNonNull(filter.getStack())) drops.add(filter.getStack());

		return drops;
	}

	@Override
	protected boolean shouldAdvance() {
		return !stack.isEmpty();
	}

	@Override
	protected void processBuffer(IItemHandler attached) {
		if (attached == null) {
			int upgradeLevel = getUpgradeLevel(Upgrade.MINING);
			if (upgradeLevel > 0) {
				if (stack.isFull()) {
					return;
				}

				World world = holder.getWorld();
				BlockPos offset = holder.getPos().offset(side);
				IBlockState state = world.getBlockState(offset);
				if (state == BlockStates.COBBLESTONE) {
					ItemStack b = new ItemStack(Blocks.COBBLESTONE, upgradeLevel);
					if (!stack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack.getStack(), b)) {
						return;
					}

					boolean lava = false;
					boolean water = false;

					EnumSet<EnumFacing> enumFacings = FacingHelper.horizontalOrthogonal.get(side);
					for (EnumFacing facing : enumFacings) {
						IBlockState blockState = world.getBlockState(offset.offset(facing));
						if (blockState == BlockStates.LAVA_LEVEL_0) {
							lava = true;
						} else if (blockState == BlockStates.WATER_LEVEL_0) {
							water = true;
						}
					}

					if (water && lava) {
						stack.insertItem(0, b, false);
					}
				} else if (state == BlockStates.SNOW_LEVEL_0) {
					ItemStack b = new ItemStack(Items.SNOWBALL, upgradeLevel * 8);
					if (!stack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack.getStack(), b)) {
						return;
					}

					if (world.getBiome(offset).getFloatTemperature(offset) >= 0.8F) {
						return;
					}

					List<EntitySnowman> entities = world.getEntitiesWithinAABB(EntitySnowman.class, new AxisAlignedBB(
							0, 0, 0, 1, 1, 1
					).offset(offset));

					for (EntitySnowman entity : entities) {
						if (!entity.isDead) {
							stack.insertItem(0, b, false);
							return;
						}
					}
				} else if (state.getBlock().isAir(state, world, offset)) {
					AxisAlignedBB aabb = new AxisAlignedBB(offset);
					aabb = aabb.union(new AxisAlignedBB(offset.offset(side, 4)));
					for (EnumFacing facing : FacingHelper.orthogonal.get(side)) {
						aabb = aabb.union(new AxisAlignedBB(offset.offset(facing, 4)));
					}
					for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, aabb)) {
						ItemStack insertItem = item.getItem();
						ItemStack result = stack.insertItem(0, insertItem, false);
						if (insertItem != result) {
							item.setItem(result);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < attached.getSlots() && !stack.isFull(); i++) {
				InventoryHelper.transferSlotAtoSlotB(attached, i, stack, 0, getMaxTransfer());
			}
		}
	}

	protected int getMaxTransfer() {
		return getUpgradeLevel(Upgrade.STACK_SIZE) > 0 ? 64 : 1;
	}

	@Override
	protected boolean processPosition(BlockPos pingPos, IItemHandler attached, IPipe pipe) {
		if (pipe == null) {
			return true;
		}

		int maxTransfer = stack.getStackLevel();

		for (EnumFacing facing : FacingHelper.getRandomFaceOrder()) {
			IItemHandler capability = pipe.getCapability(holder.getWorld(), pingPos, facing, CapGetter.ItemHandler);
			if (capability == null) continue;
			maxTransfer -= InventoryHelper.transfer(stack, 0, capability, maxTransfer, true);
			if (maxTransfer == 0) break;
		}

		if (stack.isEmpty()) {
			ping.resetPosition();
			return false;
		}


		return true;
	}

	@Override
	public boolean onActivated(EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!holder.getWorld().isRemote)
			holder.openGui(playerIn, this);
		return true;
	}

	@Override
	public IItemHandler getHandler(TileEntity tile) {
		return CapGetter.ItemHandler.getInterface(tile, side.getOpposite());
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new TransferNodeItemContainer(player);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInterface(TileEntity tileEntity, CapGetter<T> capability) {
		if (capability == CapGetter.ItemHandler) {
			final IItemHandler handler = CapGetter.ItemHandler.getInterface(tileEntity, side.getOpposite());
			if (handler != null)
				return (T) new IItemHandlerCompat() {
					public int getSlots() {
						return handler.getSlots();
					}

					@ItemStackNonNull
					public ItemStack getStackInSlot(int slot) {
						return handler.getStackInSlot(slot);
					}

					@ItemStackNonNull
					public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
						return stack;
					}

					@ItemStackNonNull
					public ItemStack extractItem(int slot, int amount, boolean simulate) {
						return StackHelper.empty();
//						ItemStack itemStack = handler.extractItem(slot, amount, true);
//						if (itemStack == null || !filter.matches(itemStack)) return null;
//						if (simulate) return itemStack;
//						return handler.extractItem(slot, amount, false);
					}
				};
		}

		return super.getInterface(tileEntity, capability);
	}

	@Override
	public GrocketType getType() {
		return GrocketType.TRANSFER_NODE_ITEMS;
	}

	@ItemStackNonNull
	@Override
	public ItemStack getItem() {
		return stack.getStack();
	}

	@Override
	public FluidStack getFluid() {
		return null;
	}

	@Override
	public Type getBufferType() {
		return Type.ITEM;
	}

	public static class Retrieve extends TransferNodeItem {
		@Override
		public GrocketType getType() {
			return GrocketType.TRANSFER_NODE_ITEMS_RETRIEVE;
		}

		@Override
		protected boolean shouldAdvance() {
			return !stack.isFull();
		}

		@Override
		protected void processBuffer(IItemHandler attached) {
			if (attached == null || stack.isEmpty()) return;
			for (int i = 0; i < attached.getSlots() && !stack.isEmpty(); i++) {
				InventoryHelper.transferSlotAtoSlotB(stack, 0, attached, i, 64);
			}
		}

		@Override
		protected boolean processPosition(BlockPos pingPos, IItemHandler attached, IPipe pipe) {
			if (pipe == null || stack.isFull()) {
				return true;
			}

			int maxTransfer = getMaxTransfer();

			for (EnumFacing facing : FacingHelper.getRandomFaceOrder()) {
				IItemHandler capability = pipe.getCapability(holder.getWorld(), pingPos, facing, CapGetter.ItemHandler);
				if (capability == null) continue;


				for (int i = 0; i < capability.getSlots() && !stack.isFull(); i++) {
					if (filter.matches(capability.getStackInSlot(i))) {
						InventoryHelper.transferSlotAtoSlotB(capability, i, stack, 0, maxTransfer);
					}
				}

				if (stack.isFull()) break;
			}

			return true;
		}
	}

	public class TransferNodeItemContainer extends DynamicContainerTile {
		public TransferNodeItemContainer(EntityPlayer player) {
			super(holder, 9, 64);
			addTitle("Transfer Node");

			int numUpgradeSlots = upgradeHandler.getSlots();
			for (int i = 0; i < numUpgradeSlots; i++) {
				addWidget(new WidgetSlotItemHandler(upgradeHandler, i, centerX + i * 18 - 9 * numUpgradeSlots, 80));
			}

			addWidget(filter.newSlot(4, 80));

			addWidget(new WidgetSlotItemHandler(stack, 0, centerSlotX, 31));
			addWidget(new WidgetPingPosition(4, 68));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}


	}

}
