package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.IItemHandlerCompat;
import com.rwtema.extrautils2.itemhandler.XUTileItemStackHandler;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileTrashChest extends XUTile implements IDynamicHandler {
	final static int BUFFER_SIZE = 9 * 3;

	private final XUTileItemStackHandler contents = registerNBT("contents", new XUTileItemStackHandler(BUFFER_SIZE, this));
	private final NBTSerializable.NBTIntArray priorities = registerNBT("priority", new NBTSerializable.NBTIntArray(new int[BUFFER_SIZE]));
	int[] ordering;
	private final IItemHandlerCompat insertion_handler = new IItemHandlerCompat() {
		@Override
		public int getSlots() {
			return BUFFER_SIZE + 20;
		}

		@ItemStackNonNull
		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot < BUFFER_SIZE) {
				return contents.getStackInSlot(slot);
			}
			return StackHelper.empty();
		}

		@ItemStackNonNull
		@Override
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			if (!simulate) {
				if (slot < BUFFER_SIZE) {
					stack = contents.insertItem(slot, stack, false);
				}
				insertStack(stack);
			}
			return StackHelper.empty();
		}

		@ItemStackNonNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot < BUFFER_SIZE) {
				return contents.extractItem(slot, amount, false);
			}
			return StackHelper.empty();
		}
	};

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return insertion_handler;
	}

	public void insertStack(ItemStack stack) {
		if (StackHelper.isEmpty(stack)) {
			return;
		}

		int fallback = -1;
		boolean hasDuplicates = false;

		for (int i = 0; i < contents.getSlots(); i++) {
			ItemStack stackInSlot = contents.getStackInSlot(i);
			if (StackHelper.isEmpty(stackInSlot)) {
				if (fallback == -1) {
					fallback = i;
				}
			} else if (ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
				hasDuplicates = true;
				stack = contents.insertItem(i, stack, false);
				if (StackHelper.isEmpty(stack)) {
					return;
				}
			}
		}

		if (fallback != -1) {
			contents.setStackInSlot(fallback, stack);
			return;
		}

		if (hasDuplicates) {
			return;
		}

		TObjectIntHashMap<ItemRef> curSlot = new TObjectIntHashMap<>();

		for (int i = 0; i < contents.getSlots(); i++) {
			ItemStack stackInSlot = contents.getStackInSlot(i);
			if (StackHelper.isEmpty(stackInSlot)) {
				contents.setStackInSlot(i, stack);
				return;
			}
			ItemRef ref = ItemRef.wrap(stackInSlot);
			if (curSlot.containsKey(ref)) {
				int j = curSlot.get(ref);
				contents.insertItem(j, stackInSlot, false);
				contents.setStackInSlot(i, stack);
				return;
			}

			curSlot.put(ref, i);
		}

		curSlot.clear();
		for (int i : (ordering = XURandom.createRandomOrder(contents.getSlots(), ordering))) {
			ItemStack stackInSlot = contents.getStackInSlot(i);
			if (StackHelper.isEmpty(stackInSlot)) {
				contents.setStackInSlot(i, stack);
				return;
			}
			ItemRef ref = ItemRef.wrapCrafting(stackInSlot);
			if (curSlot.containsKey(ref)) {
				int j = curSlot.get(ref);
				contents.insertItem(j, stack, false);
				contents.setStackInSlot(i, stack);
				return;
			}

			curSlot.put(ref, i);
		}

		int i = XURandom.rand.nextInt(BUFFER_SIZE);
		contents.setStackInSlot(i, stack);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTrashChest(this, player);
	}


	public static class ContainerTrashChest extends DynamicContainerTile {

		private TileTrashChest trashChest;

		public ContainerTrashChest(TileTrashChest tile, EntityPlayer player) {
			super(tile);
			trashChest = tile;
			addTitle(Lang.getItemName(tile.getXUBlock()), false);

			for (int j = 0; j < (BUFFER_SIZE / 9); ++j) {
				for (int k = 0; k < 9; ++k) {
					addWidget(new WidgetSlotItemHandler(tile.contents, k + j * 9, 4 + k * 18, 4 + 9 + 4 + j * 18));
				}
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}

		@ItemStackNonNull
		@Override
		public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
			ItemStack itemstack = StackHelper.empty();
			Slot slot = this.inventorySlots.get(par2);

			if (par2 >= playerSlotsStart) {
				if (slot != null && slot.getHasStack()) {
					ItemStack otherItemStack = slot.getStack();
					if (StackHelper.isNull(otherItemStack)) return StackHelper.empty();
					itemstack = otherItemStack.copy();

					if (!trashChest.world.isRemote) {
						trashChest.insertStack(otherItemStack);
					}

					slot.putStack(StackHelper.empty());
				}

				return itemstack;
			}


			return super.transferStackInSlot(par1EntityPlayer, par2);
		}
	}

}
