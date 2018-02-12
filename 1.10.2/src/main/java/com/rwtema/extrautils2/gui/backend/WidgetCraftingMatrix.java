package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.compatibility.CraftingHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class WidgetCraftingMatrix {
	public InventoryCrafting crafter;
	public InventoryCraftResult craftResult;

	public WidgetSlot[][] slots;
	public WidgetSlotCrafting slotCrafting;

	public List<IWidget> widgets;

	public WidgetCraftingMatrix(EntityPlayer player, int grid_x, int grid_y, int grid_w, int grid_h) {
		this(player, grid_x, grid_y, grid_w, grid_h, grid_x + grid_w * 18 + 22 + 8, grid_y + (grid_h - 1) * 18 / 2, grid_x + grid_w * 18 + 4, grid_y + (grid_h - 1) * 18 / 2);
	}

	public WidgetCraftingMatrix(EntityPlayer player, int grid_x, int grid_y, int grid_w, int grid_h, int result_x, int result_y, int arrow_x, int arrow_y) {
		slots = new WidgetSlot[grid_w][grid_h];
		ImmutableList.Builder<IWidget> builder = ImmutableList.builder();
		craftResult = new InventoryCraftResult();
		crafter = new XUInventoryCrafting(this, player, 3, 3);

		for (int j = 0; j < grid_h; j++) {
			for (int i = 0; i < grid_w; i++) {
				builder.add(slots[i][j] = new WidgetSlotIngredients(i, j, grid_x, grid_y, WidgetCraftingMatrix.this.crafter));
			}
		}

		slotCrafting = new WidgetSlotCrafting(player, crafter, craftResult, result_x, result_y);
		builder.add(slotCrafting);
		builder.add(new WidgetProgressArrowBase(arrow_x, arrow_y));
		widgets = builder.build();
	}

	public static class WidgetSlotCrafting extends SlotCrafting implements IWidget {
		private final int x;
		private final int y;

		public WidgetSlotCrafting(EntityPlayer player, InventoryCrafting craftingInventory, InventoryCraftResult inventoryIn, int x, int y) {
			super(player, craftingInventory, inventoryIn, 0, x + 1, y + 1);
			this.x = x;
			this.y = y;
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
		@SideOnly(Side.CLIENT)
		public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
		}


		@Override
		public List<String> getToolTip() {
			return null;
		}


		@Override
		public void addToContainer(DynamicContainer container) {
			container.addSlot(this);
		}
	}

	public static class WidgetSlotIngredients extends WidgetSlot {
		public final int index;
		public InventoryCrafting crafter;

		public WidgetSlotIngredients(int i, int j, int grid_x, int grid_y, InventoryCrafting crafter) {
			super(crafter, i + j * 3, grid_x + i * 18, grid_y + j * 18);
			this.crafter = crafter;
			index = i + j * 3;
		}

		@Override
		public void onContainerClosed(DynamicContainer container, EntityPlayer playerIn) {
			if (!playerIn.world.isRemote) {
				ItemStack itemStack = crafter.removeStackFromSlot(getSlotIndex());
				if (StackHelper.isNonNull(itemStack)) {
					playerIn.dropItem(itemStack, false);
				}
			}
		}

		@Override
		public void addToContainer(DynamicContainer container) {
			super.addToContainer(container);
		}
	}

	public static class XUInventoryCrafting extends InventoryCrafting {
		public EntityPlayer player;

		public XUInventoryCrafting(WidgetCraftingMatrix widgetCraftingMatrix, EntityPlayer player, int width, int height) {
			super(new Container() {
				@Override
				public void onCraftMatrixChanged(IInventory inventoryIn) {
					widgetCraftingMatrix.craftResult.setInventorySlotContents(0, CraftingHelper112.getMatchingResult(widgetCraftingMatrix, player));
				}

				@Override
				public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
					return false;
				}
			}, width, height);
			this.player = player;
		}
	}

}
