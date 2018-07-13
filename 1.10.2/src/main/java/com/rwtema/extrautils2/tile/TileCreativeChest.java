package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.IItemHandlerModifiableCompat;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerBase;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileCreativeChest extends XUTile implements IDynamicHandler {
	public NBTSerializable.NBTStack heldStack = registerNBT("held_stack", new NBTSerializable.NBTStack());

	public IItemHandler handler = new IItemHandlerModifiableCompat() {
		@Override
		public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {

		}

		@Override
		public int getSlots() {
			return 1;
		}

		@ItemStackNonNull
		@Override
		public ItemStack getStackInSlot(int slot) {
			ItemStack raw = heldStack.getRaw();
			if (StackHelper.isNull(raw)) return StackHelper.empty();
			return ItemHandlerHelper.copyStackWithSize(raw, raw.getMaxStackSize());
		}

		@ItemStackNonNull
		@Override
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			return stack;
		}

		@ItemStackNonNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack raw = heldStack.getRaw();
			if (StackHelper.isNull(raw)) return StackHelper.empty();
			return heldStack.getCopy(Math.min(amount, raw.getMaxStackSize()));
		}
	};

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return handler;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCreativeChest(this, player);
	}

	public static class ContainerCreativeChest extends DynamicContainerTile {

		public ContainerCreativeChest(TileCreativeChest tileCreativeChest, EntityPlayer player) {
			super(tileCreativeChest);
			addTitle(Lang.getItemName(XU2Entries.creativeChest.value));
			crop();

			int arrow_w = WidgetProgressArrowBase.ARROW_WIDTH / 2;
			addWidget(new WidgetProgressArrowBase(centerX - arrow_w, height));

			addWidget(new WidgetSlotItemHandler(new SingleStackHandlerBase() {
				@ItemStackNonNull
				@Override
				public ItemStack getStack() {
					ItemStack raw = tileCreativeChest.heldStack.getRaw();
					if (StackHelper.isNull(raw)) return StackHelper.empty();
					return ItemHandlerHelper.copyStackWithSize(raw, raw.getMaxStackSize());
				}

				@Override
				public void setStack(@ItemStackNonNull ItemStack stack) {
					tileCreativeChest.heldStack.setStackRaw(stack);
				}
			}, 0, centerX - arrow_w - 4 - 18, height) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return player.capabilities.isCreativeMode && super.isItemValid(stack);
				}

				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return player.capabilities.isCreativeMode && super.canTakeStack(playerIn);
				}

				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					if (!player.capabilities.isCreativeMode)
						GlStateManager.color(0.5F, 0.5F, 0.5F);
					gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
					GlStateManager.color(1, 1, 1);
				}
			});

			addWidget(new WidgetSlotItemHandler(tileCreativeChest.handler, 0, centerX + arrow_w + 4, height) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return false;
				}

				@Override
				public int getItemStackLimit(ItemStack stack) {
					return 0;
				}

				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					if (!getHasStack())
						GlStateManager.color(0.5F, 0.5F, 0.5F);
					gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
					GlStateManager.color(1, 1, 1);
				}
			});

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
