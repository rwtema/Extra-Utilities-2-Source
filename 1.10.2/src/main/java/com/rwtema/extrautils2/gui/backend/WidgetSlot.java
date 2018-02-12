package com.rwtema.extrautils2.gui.backend;


import java.util.List;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WidgetSlot extends Slot implements IWidget {
	boolean isISided;
	private int x;
	private int y;
	int side;

	public WidgetSlot(IInventory inv, int slot, int x, int y) {
		super(inv, slot, x + 1, y + 1);
		isISided = inv instanceof ISidedInventory;
		this.x = x;
		this.y = y;
		side = 0;

		if (isISided) {
			for (side = 0; side < 6; side++) {
				int[] slots = ((ISidedInventory) inv).getSlotsForFace(EnumFacing.values()[side]);

				for (int s : slots) {
					if (s == slot) {
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		return inventory.isItemValidForSlot(getSlotIndex(), par1ItemStack);
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
		return true;
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
		container.addSlot(this);
	}

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
