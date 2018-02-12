package com.rwtema.extrautils2.itemhandler;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.api.fluids.IFluidFilter;
import com.rwtema.extrautils2.api.items.IItemFilter;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.gui.backend.ITransferPriority;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.transfernodes.IBuffer;
import com.rwtema.extrautils2.utils.Lang;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class SingleStackHandlerFilter extends SingleStackHandler {
	@Override
	protected abstract int getStackLimit(@Nonnull ItemStack stack);

	public WidgetSlotItemHandler newSlot(final int x, final int y) {
		return new WidgetSlotFilter(this, x, y);
	}

	public abstract Item getExpectedItem();

	public boolean hasFilter() {
		return StackHelper.isNonNull(getStack());
	}

	public static class FluidFilter extends SingleStackHandlerFilter {
		public boolean matches(FluidStack stack) {
			ItemStack filterStack = getStack();
			if (StackHelper.isNull(filterStack)) return true;
			Item item = filterStack.getItem();
			return item instanceof IFluidFilter && ((IFluidFilter) item).matches(filterStack, stack);
		}

		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			Item item = stack.getItem();
			return item instanceof IFluidFilter && ((IFluidFilter) item).isFluidFilter(stack) ? stack.getMaxStackSize() : 0;
		}

		@Override
		public Item getExpectedItem() {
			return XU2Entries.filterFluids.value;
		}
	}

	public static class EitherFilter extends SingleStackHandlerFilter {
		public boolean matches(ItemStack stack) {
			ItemStack filterStack = getStack();

			if (StackHelper.isNull(filterStack)) return true;
			Item item = filterStack.getItem();
			return item instanceof IItemFilter && ((IItemFilter) item).matches(filterStack, stack);
		}

		public boolean matches(FluidStack stack) {
			ItemStack filterStack = getStack();
			if (StackHelper.isNull(filterStack)) return true;
			Item item = filterStack.getItem();
			return item instanceof IFluidFilter && ((IFluidFilter) item).matches(filterStack, stack);
		}

		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			Item item = stack.getItem();
			if (item instanceof IItemFilter && ((IItemFilter) item).isItemFilter(stack) ||
					item instanceof IFluidFilter && ((IFluidFilter) item).isFluidFilter(stack))
				return stack.getMaxStackSize();
			else return 0;
		}

		@Override
		public Item getExpectedItem() {
			if ((System.currentTimeMillis() % 2000) < 1000) {
				return XU2Entries.filterFluids.value;
			} else
				return XU2Entries.filterItems.value;
		}

		public boolean matches(IBuffer buffer) {
			switch (buffer.getBufferType()) {
				case FLUID:
					return matches(buffer.getFluid());
				case ITEM:
					return matches(buffer.getItem());
			}
			return false;
		}
	}

	public static class ItemFilter extends SingleStackHandlerFilter {
		public boolean matches(ItemStack stack) {
			ItemStack filterStack = getStack();

			if (StackHelper.isNull(filterStack)) return true;
			Item item = filterStack.getItem();
			return item instanceof IItemFilter && ((IItemFilter) item).matches(filterStack, stack);
		}

		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			return isItemFilter(stack) ? stack.getMaxStackSize() : 0;
		}

		public boolean isItemFilter(ItemStack stack) {
			Item item;
			return StackHelper.isNonNull(stack) && (item = stack.getItem()) instanceof IItemFilter && ((IItemFilter) item).isItemFilter(stack);
		}

		@Override
		public Item getExpectedItem() {
			return XU2Entries.filterItems.value;
		}
	}

	public static class WidgetSlotFilter extends WidgetSlotItemHandler implements ITransferPriority {
		protected SingleStackHandlerFilter filter;

		public WidgetSlotFilter(SingleStackHandlerFilter filter, int x, int y) {
			super(filter, 0, x, y);
			this.filter = filter;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public List<String> getToolTip() {
			if (!getHasStack()) {
				return ImmutableList.of(Lang.getItemName(filter.getExpectedItem()));
			}
			return null;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
			super.renderBackground(manager, gui, guiLeft, guiTop);
			if (!getHasStack()) {
				ItemStack stack = ItemIngredients.Type.FILTER_SKELETON.newStack();
				gui.renderStack(stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
			}
		}

		@Override
		public int getTransferPriority() {
			return 1;
		}
	}
}
