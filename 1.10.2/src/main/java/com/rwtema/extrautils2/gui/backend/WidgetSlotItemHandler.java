package com.rwtema.extrautils2.gui.backend;

import java.util.List;

import com.rwtema.extrautils2.itemhandler.IItemHandlerUpdate;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public class WidgetSlotItemHandler extends SlotItemHandler implements IWidget {
	private IItemHandler itemHandler;
	protected final int index;
	private int x;
	private int y;
	private DynamicContainer container;

	public WidgetSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition + 1, yPosition + 1);
		this.itemHandler = itemHandler;
		this.index = index;
		x = xPosition;
		y = yPosition;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getW() {
		return 18;
	}

	@Override
	public int getH() {
		return 18;
	}

	@Override
	public void addToContainer(DynamicContainer container) {
		this.container = container;
		container.addSlot(this);
	}

	@Override
	public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
		super.onSlotChange(p_75220_1_, p_75220_2_);
		container.onSlotChanged(slotNumber);
		if(itemHandler instanceof IItemHandlerUpdate){
			((IItemHandlerUpdate) itemHandler).onChange(index);
		}
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		container.onSlotChanged(slotNumber);
		if(itemHandler instanceof IItemHandlerUpdate){
			((IItemHandlerUpdate) itemHandler).onChange(index);
		}
	}
//
//	@Override
//	public int getItemStackLimit(ItemStack stack) {
//		IItemHandler handler = getItemHandler();
//		if (handler instanceof IItemHandlerModifiable) {
//			IItemHandlerModifiable modifiable = (IItemHandlerModifiable) handler;
//			ItemStack prevStack = modifiable.getStackInSlot(index);
//			modifiable.setStackInSlot(index, null);
//
//			stack = stack.copy();
//			int limit = stack.getMaxStackSize();
//			stack.stackSize = limit;
//			ItemStack itemStack = modifiable.insertItem(index, stack, true);
//			if (itemStack != null) limit -= itemStack.stackSize;
//
//			modifiable.setStackInSlot(index, prevStack);
//			return limit;
//		} else
//			return super.getItemStackLimit(stack);
//	}
//
//	@Override
//	public boolean isItemValid(ItemStack stack) {
//		IItemHandler handler = getItemHandler();
//		if (handler instanceof IItemHandlerModifiable) {
//			IItemHandlerModifiable modifiable = (IItemHandlerModifiable) handler;
//			ItemStack prevStack = modifiable.getStackInSlot(index);
//			modifiable.setStackInSlot(index, null);
//			ItemStack remainder = modifiable.insertItem(index, stack, true);
//			modifiable.setStackInSlot(index, prevStack);
//			return remainder == null || remainder.stackSize < stack.stackSize;
//		} else
//			return super.isItemValid(stack);
//	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
	}


	@Override
	public List<String> getToolTip() {
		return null;
	}

	@Override
	public void addToGui(DynamicGui gui) {

	}
}
