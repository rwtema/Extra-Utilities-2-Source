package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.IItemHandlerModifiableCompat;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileTrashCan extends XUTile implements IDynamicHandler {
	SingleStackHandlerFilter.ItemFilter FILTER = registerNBT("filter", new SingleStackHandlerFilter.ItemFilter());

	IItemHandler ABSORB_HANDLER = new IItemHandlerModifiableCompat() {
		@Override
		public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {

		}

		@Override
		public int getSlots() {
			return 32;
		}

		@ItemStackNonNull
		@Override
		public ItemStack getStackInSlot(int slot) {
			return StackHelper.empty();
		}

		@ItemStackNonNull
		@Override
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			return StackHelper.isNull(stack) || FILTER.isEmpty() || FILTER.matches(stack) ? StackHelper.empty() : stack;
		}

		@ItemStackNonNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return StackHelper.empty();
		}
	};



	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(FILTER);
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return ABSORB_HANDLER;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTrashCan(player);
	}

	public class ContainerTrashCan extends DynamicContainerTile {

		public ContainerTrashCan(EntityPlayer player) {
			super(TileTrashCan.this, 16, 64);
			addTitle(Lang.getItemName(getXUBlock()), false);
			addWidget(FILTER.newSlot(playerInvWidth - 18, 40));
			addWidget(new WidgetSlotItemHandler(ABSORB_HANDLER, 0, centerSlotX, 40));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
