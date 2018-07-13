package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.datastructures.ArrayAccess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.List;

public class XUCrafter extends InventoryCrafting {

	public static final Container DUMMY_CONTAINER = new Container() {
		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			return false;
		}
	};
	private final ArrayAccess<ItemStack> array10List11;

	public XUCrafter() {
		this(3, 3);
	}

	public XUCrafter(int width, int height) {
		super(DUMMY_CONTAINER, width, height);
		array10List11 = CompatHelper.getArray10List11(stackList);

	}

	public void loadStacks(ItemStack stacks[]) {
		Validate.isTrue(stacks.length == array10List11.length());
		for (int i = 0; i < array10List11.length(); i++) {
			array10List11.set(i, stacks[i] == null ? StackHelper.empty() : stacks[i]);
		}
	}

	public void loadStacks(List<ItemStack> stacks) {
		ArrayAccess<ItemStack> arrayAccess = array10List11;
		Validate.isTrue(stacks.size() == arrayAccess.length());
		for (int i = 0; i < arrayAccess.length(); i++) {
			ItemStack stack = stacks.get(i);
			arrayAccess.set(i, stack == null ? StackHelper.empty() : stack);
		}
	}

	@Override
	public void setInventorySlotContents(int index, @ItemStackNonNull ItemStack stack) {
		array10List11.set(index, stack);
	}

	@Override
	@ItemStackNonNull
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.stackList, index, count);
	}

	public void clear() {
		ArrayAccess<ItemStack> stacks = array10List11;
		for (int i = 0; i < stacks.length(); i++) {
			stacks.set(i, StackHelper.empty());
		}
	}

	public void loadStacks(IItemHandler recipeSlots) {
		for (int i = 0; i < recipeSlots.getSlots(); i++) {
			array10List11.set(i, recipeSlots.getStackInSlot(i));
		}
	}

	public void loadStacks(XUCrafter crafter) {
		loadStacks(crafter.stackList);
	}
}
