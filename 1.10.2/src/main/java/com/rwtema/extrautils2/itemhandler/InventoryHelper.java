package com.rwtema.extrautils2.itemhandler;

import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.compatibility.StackHelper;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class InventoryHelper {
	private static final Random RANDOM = new Random();

	public static void dropAll(World world, BlockPos pos, Iterable<ItemStack> stacks) {
		dropAll(world, pos.getX(), pos.getY(), pos.getZ(), stacks);
	}

	public static void dropAll(World world, int x, int y, int z, Iterable<ItemStack> stacks) {
		for (ItemStack stack : stacks) {
			if (StackHelper.isNonNull(stack))
				dropItemStack(world, x, y, z, stack);
		}
	}

	public static void dropItemStack(World worldIn, int x, int y, int z, ItemStack stack) {
		float dx = x + RANDOM.nextFloat() * 0.8F + 0.1F;
		float dy = y + RANDOM.nextFloat() * 0.8F + 0.1F;
		float dz = z + RANDOM.nextFloat() * 0.8F + 0.1F;

		dropItemStackAtPosition(worldIn, stack, dx, dy, dz);
	}

	public static void dropItemStackAtPosition(World worldIn, ItemStack stack, double dx, double dy, double dz) {
		EntityItem entityitem = new EntityItem(worldIn, dx, dy, dz, new ItemStack(stack.getItem(), StackHelper.getStacksize(stack), stack.getMetadata()));

		if (stack.hasTagCompound()) {
			entityitem.getItem().setTagCompound(stack.getTagCompound().copy());
		}

		entityitem.motionX = RANDOM.nextGaussian() * 0.05;
		entityitem.motionY = RANDOM.nextGaussian() * 0.05 + 0.2;
		entityitem.motionZ = RANDOM.nextGaussian() * 0.05;
		worldIn.spawnEntity(entityitem);
	}

	public static int transfer(IItemHandler src, int srcSlot, IItemHandler dest, int maxAmount, boolean allSlots) {

		int sent = 0;
		maxAmount = getStackLimit(src.extractItem(srcSlot, maxAmount, true));

		if (maxAmount == 0) return sent;

		int firstEmptySlot = -1;

		for (int i = 0; i < dest.getSlots(); i++) {
			if (StackHelper.isNonNull(dest.getStackInSlot(i))) {
				sent += transferSlotAtoSlotB(src, srcSlot, dest, i, maxAmount - sent);
				if ((sent > 0 && allSlots) || sent >= maxAmount) {
					return sent;
				}
			} else if (firstEmptySlot == -1) firstEmptySlot = i;
		}

		if (firstEmptySlot == -1) return sent;

		for (int i = firstEmptySlot; i < dest.getSlots(); i++) {
			if (StackHelper.isNull(dest.getStackInSlot(i))) {
				sent += transferSlotAtoSlotB(src, srcSlot, dest, i, maxAmount - sent);
				if ((sent > 0 && allSlots) || sent >= maxAmount) {
					return sent;
				}
			}
		}

		return sent;
	}

	public static int getStackLimit(ItemStack stack) {
		return StackHelper.isNonNull(stack) ? StackHelper.getStacksize(stack) : 0;
	}

	public static int transferSlotAtoSlotB(IItemHandler handlerA, int slotA, IItemHandler handlerB, int slotB, int maxAmount) {
		ItemStack initExtract = handlerA.extractItem(slotA, maxAmount, true);
		if (StackHelper.isNull(initExtract)) return 0;
		ItemStack initRemainder = handlerB.insertItem(slotB, initExtract, true);
		int i = StackHelper.getStacksize(initExtract) - getStackLimit(initRemainder);
		if (i == 0) return 0;
		ItemStack actualExtract = handlerA.extractItem(slotA, i, false);
		handlerB.insertItem(slotB, actualExtract, false);
		return i;
	}

	public static void insertWithRunoff(IItemHandler dest, ItemStack stack, StackDump runoff) {
		if (StackHelper.isNull(stack)) return;
		ItemStack insert = insert(dest, stack, false);
		if (StackHelper.isNonNull(insert)) {
			runoff.addStack(insert);
		}
	}

	public static ItemStack insert(IItemHandler handler, ItemStack insert, boolean simulate) {
		int firstEmptySlot = -1;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stackInSlot = handler.getStackInSlot(i);
			if (StackHelper.isNonNull(stackInSlot)) {
				if (ItemHandlerHelper.canItemStacksStack(stackInSlot, insert)) {
					insert = handler.insertItem(i, insert, simulate);
					if (StackHelper.isNull(insert)) return StackHelper.empty();
				}
			} else if (firstEmptySlot == -1)
				firstEmptySlot = i;
		}

		if (firstEmptySlot == -1) return insert;

		for (int i = firstEmptySlot; i < handler.getSlots(); i++) {
			ItemStack stackInSlot = handler.getStackInSlot(i);
			if (StackHelper.isNull(stackInSlot)) {
				insert = handler.insertItem(i, insert, simulate);
				if (StackHelper.isNull(insert)) return StackHelper.empty();
			}
		}
		return insert;
	}

	public static int getMaxInsert(IItemHandler handler, ItemStack insert) {
		if (StackHelper.isNull(insert)) return 0;
		int adding = StackHelper.getStacksize(insert);
		for (int i = 0; i < handler.getSlots(); i++) {
			insert = handler.insertItem(i, insert, true);
			if (StackHelper.isNull(insert)) return adding;
		}

		return adding - StackHelper.getStacksize(insert);
	}

	public static IItemHandler copyHandlerWithProperties(final IItemHandler handler) {
		int slots = handler.getSlots();
		ItemStackHandler copy = new ItemStackHandler(slots) {
			TIntHashSet dirtySlots = new TIntHashSet();

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				if (simulate && !dirtySlots.contains(slot))
					return handler.insertItem(slot, stack, true);

				if (handler.insertItem(slot, stack, true) == stack) return stack;

				ItemStack item = super.insertItem(slot, stack, simulate);
				if (item != stack) {
					dirtySlots.add(slot);
				}
				return item;
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (simulate && !dirtySlots.contains(slot))
					return handler.extractItem(slot, amount, true);

				if (StackHelper.isNull(handler.extractItem(slot, amount, true))) return StackHelper.empty();

				ItemStack item = super.extractItem(slot, amount, simulate);
				if (StackHelper.isNonNull(item)) {
					dirtySlots.add(slot);
				}
				return item;
			}
		};
		for (int i = 0; i < slots; i++) {
			ItemStack stackInSlot = handler.getStackInSlot(i);
			if (StackHelper.isNonNull(stackInSlot)) stackInSlot = stackInSlot.copy();
			copy.setStackInSlot(i, stackInSlot);
		}

		return copy;
	}

	public static IItemHandler copyHandler(IItemHandler handler) {
		int slots = handler.getSlots();
		ItemStackHandler copy = new ItemStackHandler(slots);
		for (int i = 0; i < slots; i++) {
			ItemStack stackInSlot = handler.getStackInSlot(i);
			if (StackHelper.isNonNull(stackInSlot)) stackInSlot = stackInSlot.copy();
			copy.setStackInSlot(i, stackInSlot);
		}

		return copy;
	}

	public static Iterable<ItemStack> getItemHandlerIterator(IItemHandler... handler) {
		List<Iterable<ItemStack>> totalList = new ArrayList<>(handler.length);
		for (IItemHandler iItemHandler : handler) {
			totalList.add(getItemHandlerIterator(iItemHandler));
		}

		return Iterables.concat(totalList);
	}

	public static Iterable<ItemStack> getItemHandlerIterator(final IItemHandler handler) {
		return new Iterable<ItemStack>() {
			@Override
			public Iterator<ItemStack> iterator() {
				return new Iterator<ItemStack>() {
					int slot = 0;

					@Override
					public boolean hasNext() {
						return slot < handler.getSlots();
					}

					@Override
					public ItemStack next() {
						return handler.getStackInSlot(slot++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}
