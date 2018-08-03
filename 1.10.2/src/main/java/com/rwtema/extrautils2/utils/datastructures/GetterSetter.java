package com.rwtema.extrautils2.utils.datastructures;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface GetterSetter<T> extends Consumer<T>, Supplier<T> {

	class ContainerSlot implements GetterSetter<ItemStack> {
		final Slot slot;

		public ContainerSlot(Slot slot) {
			this.slot = slot;
		}

		@Override
		public void accept(ItemStack stack) {
			this.slot.putStack(stack);
		}

		@Override
		public ItemStack get() {
			return this.slot.getHasStack() ? slot.getStack() : StackHelper.empty();
		}
	}

	class InvSlot implements GetterSetter<ItemStack> {
		final IInventory inventory;
		final int slot;

		public InvSlot(IInventory inventory, int slot) {
			this.inventory = inventory;
			this.slot = slot;
		}

		@Override
		public void accept(ItemStack stack) {
			inventory.setInventorySlotContents(slot, stack);
		}

		@Override
		public ItemStack get() {
			return inventory.getStackInSlot(slot);
		}
	}

	class PlayerHand implements GetterSetter<ItemStack> {
		final InventoryPlayer inventoryPlayer;

		public PlayerHand(InventoryPlayer inventoryPlayer) {
			this.inventoryPlayer = inventoryPlayer;
		}

		@Override
		public void accept(ItemStack stack) {
			inventoryPlayer.setItemStack(stack);
		}

		@Override
		public ItemStack get() {
			return inventoryPlayer.getItemStack();
		}
	}
}
