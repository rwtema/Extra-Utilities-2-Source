package com.rwtema.extrautils2.machine;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.ConcatItemHandler;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.PublicWrapper;
import com.rwtema.extrautils2.itemhandler.XUTileItemStackHandler;
import com.rwtema.extrautils2.tile.RedstoneState;
import com.rwtema.extrautils2.tile.TileAdvInteractor;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class TileGrouper extends TileAdvInteractor implements IDynamicHandler, ITickable {
	private final static int SLOT_COUNT = 9;
	private XUTileItemStackHandler ghostSlots = registerNBT("ghost_stacks", new XUTileItemStackHandler(SLOT_COUNT, this));
	private XUTileItemStackHandler inputs = registerNBT("inputs", new XUTileItemStackHandler(SLOT_COUNT, this) {
		@Override
		protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
			ItemStack ghostStack = ghostSlots.getStackInSlot(slot);
			if (StackHelper.isEmpty(ghostStack) ||
					!ItemHandlerHelper.canItemStacksStackRelaxed(stack, ghostStack))
				return 0;
			return super.getStackLimit(slot, stack);
		}
	});

	private XUTileItemStackHandler outputs = registerNBT("outputs", new XUTileItemStackHandler(SLOT_COUNT, this));
	private NBTSerializable.NBTEnum<State> state = registerNBT("state", new NBTSerializable.NBTEnum<>(State.INSERTION));

	private IItemHandler handler = new ConcatItemHandler(new PublicWrapper.Insert(inputs), new PublicWrapper.Extract(outputs) {
		@ItemStackNonNull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (state.value != State.EXTRACTION) return StackHelper.empty();
			ItemStack stack = handler.extractItem(slot, amount, simulate);
			if (!simulate) {
				for (int i = 0; i < outputs.getSlots(); i++) {
					if (!StackHelper.isEmpty(outputs.getStackInSlot(i))) {
						return stack;
					}
				}
				cooldown.value = 20;
				state.value = State.INSERTION;
			}
			return stack;
		}
	});

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerGrouper(this, player);
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(inputs, outputs);
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return handler;
	}

	@Override
	protected void operate() {
		if (state.value == State.INSERTION) {
			boolean flag = false;
			for (int i = 0; i < SLOT_COUNT; i++) {
				ItemStack ghostStack = ghostSlots.getStackInSlot(i);
				int stacksize = StackHelper.getStacksize(ghostStack);
				flag = flag || stacksize > 0;
				if (stacksize > 0 && (StackHelper.getStacksize(inputs.getStackInSlot(i)) < stacksize || !ItemHandlerHelper.canItemStacksStackRelaxed(inputs.getStackInSlot(i), ghostStack))) {
					if (redstone_state.value == RedstoneState.OPERATE_REDSTONE_PULSE) {
						pulses.value++;
					}
					return;
				}
			}

			for (int i = 0; i < SLOT_COUNT; i++) {
				int stacksize = StackHelper.getStacksize(ghostSlots.getStackInSlot(i));
				outputs.insertItem(i, inputs.extractItem(i, stacksize, false), false);
			}
			state.value = State.EXTRACTION;
		} else {
			for (int i = 0; i < SLOT_COUNT; i++) {
				if (!StackHelper.isEmpty(outputs.getStackInSlot(i))) {
					return;
				}
			}
			state.value = State.INSERTION;
		}
	}

	enum State {
		INSERTION,
		EXTRACTION
	}

	public static class ContainerGrouper extends DynamicContainerTile {

		public ContainerGrouper(TileGrouper tileGrouper, EntityPlayer player) {
			super(tileGrouper);
			addTitle(tileGrouper);
			crop();
			addWidget(new WidgetTextTranslate(4, height, Lang.translate("Input Inventory"), DynamicContainer.playerInvWidth));
			crop();
			for (int i = 0; i < SLOT_COUNT; i++) {
				addWidget(
						new WidgetSlotItemHandler(tileGrouper.inputs, i, centerSlotX + (18 / 2 + 18 * i - 18 * SLOT_COUNT / 2), height) {
							@Override
							public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
								if (getHasStack()) {
									ItemStack ghostStack = tileGrouper.ghostSlots.getStackInSlot(index);
									if (StackHelper.isEmpty(ghostStack) || !ItemHandlerHelper.canItemStacksStackRelaxed(getStack(), ghostStack))
										GlStateManager.color(1F, 0, 0);
								} else {
									ItemStack ghostStack = tileGrouper.ghostSlots.getStackInSlot(index);
									if (StackHelper.isEmpty(ghostStack))
										GlStateManager.color(0.5F, 0.5F, 0.5f);
								}
								super.renderBackground(manager, gui, guiLeft, guiTop);

								GlStateManager.color(1, 1, 1);
							}
						});
			}
			crop();
			addWidget(new WidgetTextTranslate(4, height, Lang.translate("Required Items"), DynamicContainer.playerInvWidth));
			crop();

			for (int i = 0; i < SLOT_COUNT; i++) {
				addWidget(
						new WidgetSlotGhost(tileGrouper.ghostSlots, i, centerSlotX + (18 / 2 + 18 * i - 18 * SLOT_COUNT / 2), height) {
							@Override
							public void putStack(ItemStack stack) {
								((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, StackHelper.safeCopy(stack));
								this.onSlotChanged();
							}
						});
			}
			crop();
			addWidget(new WidgetTextTranslate(4, height, Lang.translate("Output Inventory"), DynamicContainer.playerInvWidth));
			crop();

			for (int i = 0; i < SLOT_COUNT; i++) {
				addWidget(
						new WidgetSlotItemHandler(tileGrouper.outputs, i, centerSlotX + (18 / 2 + 18 * i - 18 * SLOT_COUNT / 2), height) {
							@Override
							public boolean isItemValid(@Nonnull ItemStack stack) {
								return false;
							}

							@Override
							public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
								if (!getHasStack()) {
									ItemStack ghostStack = tileGrouper.ghostSlots.getStackInSlot(index);
									if (StackHelper.isEmpty(ghostStack))
										GlStateManager.color(0.5F, 0.5F, 0.5f);
								}
								super.renderBackground(manager, gui, guiLeft, guiTop);

								GlStateManager.color(1, 1, 1);
							}
						});
			}
			crop();
			addWidget(tileGrouper.upgrades.getSpeedUpgradeSlot(centerX - 18 - 4, height + 4));
			addWidget(getRSWidget(centerX + 4, height + 4, tileGrouper.redstone_state, tileGrouper.pulses));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
