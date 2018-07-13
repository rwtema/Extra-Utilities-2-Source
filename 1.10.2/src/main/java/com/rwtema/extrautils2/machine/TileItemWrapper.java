package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.IItemFluidHandlerCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.IItemHandlerCompat;
import com.rwtema.extrautils2.itemhandler.IItemHandlerModifiableCompat;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileItemWrapper extends XUTile implements IDynamicHandler {
	public static final IEnergyStorage EMPTY_STORAGE = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	};
	public SingleStackHandler stackSlot = registerNBT("stack", new SingleStackHandler());
	public IEnergyStorage energyHandler = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return getStorage().receiveEnergy(maxReceive, simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return getStorage().extractEnergy(maxExtract, simulate);
		}

		@Override
		public int getEnergyStored() {
			return getStorage().getEnergyStored();
		}

		@Override
		public int getMaxEnergyStored() {
			return getStorage().getMaxEnergyStored();
		}

		@Override
		public boolean canExtract() {
			return getStorage().canExtract();
		}

		@Override
		public boolean canReceive() {
			return getStorage().canReceive();
		}

		@Nonnull
		public IEnergyStorage getStorage() {
			ItemStack stack = stackSlot.getStack();
			if (StackHelper.isEmpty(stack)) return EMPTY_STORAGE;
			IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (energyStorage == null) return EMPTY_STORAGE;
			return energyStorage;

		}

	};
	public IFluidHandler fluidHandler = new IFluidHandler() {
		@Override
		public IFluidTankProperties[] getTankProperties() {
			return getFluidHandler().getTankProperties();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (doFill) {
				IItemFluidHandlerCompat fluidHandler = getFluidHandler();
				int fill = fluidHandler.fill(resource, true);
				stackSlot.setStack(fluidHandler.getModifiedStack());
				markDirty();
				return fill;
			}
			return getFluidHandler().fill(resource, false);
		}

		@Override
		@Nullable
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if (doDrain) {
				IItemFluidHandlerCompat fluidHandler = getFluidHandler();
				FluidStack drain = fluidHandler.drain(resource, true);
				stackSlot.setStack(fluidHandler.getModifiedStack());
				markDirty();
				return drain;
			}
			return getFluidHandler().drain(resource, false);
		}

		@Override
		@Nullable
		public FluidStack drain(int maxDrain, boolean doDrain) {
			if (doDrain) {
				IItemFluidHandlerCompat fluidHandler = getFluidHandler();
				FluidStack drain = fluidHandler.drain(maxDrain, true);
				stackSlot.setStack(fluidHandler.getModifiedStack());
				markDirty();
				return drain;
			}
			return getFluidHandler().drain(maxDrain, false);
		}

		public IItemFluidHandlerCompat getFluidHandler() {

			ItemStack stack = stackSlot.getStack();
			if (StackHelper.isEmpty(stack)) return wrap(stack, EmptyFluidHandler.INSTANCE);
			IItemFluidHandlerCompat itemCap = IItemFluidHandlerCompat.getFluidHandler(stack);
			if (itemCap != null) return itemCap;
			IFluidHandler fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
			if (fluidHandler != null) return wrap(stack, fluidHandler);
			return wrap(stack, EmptyFluidHandler.INSTANCE);
		}

		private IItemFluidHandlerCompat wrap(ItemStack stack, IFluidHandler instance) {
			return new IItemFluidHandlerCompat() {
				@Nonnull
				@Override
				public ItemStack getModifiedStack() {
					return stack;
				}

				@Override
				public IFluidTankProperties[] getTankProperties() {
					return instance.getTankProperties();
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {
					return instance.fill(resource, doFill);
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {
					return instance.drain(resource, doDrain);
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {
					return instance.drain(maxDrain, doDrain);
				}
			};
		}
	};
	public IItemHandlerModifiableCompat itemHandler = new IItemHandlerModifiableCompat() {
		@Override
		public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
			if (slot == 0) {
				stackSlot.setStackInSlot(0, stack);
			} else {
				IItemHandler handler = getHandler();
				if (handler instanceof IItemHandlerModifiable) {
					((IItemHandlerModifiable) handler).setStackInSlot(slot - 1, stack);
				}
			}
		}

		@Override
		public int getSlots() {
			return getHandler().getSlots() + 1;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot == 0) {
				return stackSlot.getStack();
			} else {
				return getHandler().getStackInSlot(slot - 1);
			}
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			markDirty();
			if (slot == 0) {
				return stackSlot.insertItem(0, stack, simulate);
			} else {
				return getHandler().insertItem(slot - 1, stack, simulate);
			}
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			markDirty();
			if (slot == 0) {
				return stackSlot.extractItem(0, amount, simulate);
			} else {
				return getHandler().extractItem(slot - 1, amount, simulate);
			}
		}

		@Nonnull
		public IItemHandler getHandler() {
			ItemStack stack = TileItemWrapper.this.stackSlot.getStack();
			if (StackHelper.isEmpty(stack)) return EmptyHandler.INSTANCE;

			IItemHandler capability = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (capability == null) return EmptyHandler.INSTANCE;
			return capability;
		}
	};

	@Nullable
	@Override
	public IFluidHandler getFluidHandler(EnumFacing facing) {
		return fluidHandler;
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(stackSlot);
	}

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return energyHandler;
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return itemHandler;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerWrapper(this, player);
	}

	public static class ContainerWrapper extends DynamicContainerTile {
		public ContainerWrapper(TileItemWrapper tile, EntityPlayer player) {
			super(tile);
			final int y = 18;
			final int y_dist = 12;
			addWidget(new WidgetSlotItemHandler(tile.stackSlot, 0, centerSlotX, y));
			final int x = centerSlotX + 18 + 4;
			addTitle(tile);
			addWidget(new WidgetProgresSHorizScrollColored(Lang.translate("Contained Inventory"), x, y, 0xFFD800) {
				@Override
				public int[] getData() {
					ItemStack stack = tile.stackSlot.getStack();
					if (StackHelper.isEmpty(stack))
						return null;

					IItemHandler capability = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					//noinspection ConstantConditions
					if (capability == null) return null;

					IItemHandlerCompat itemHandler = CompatHelper.wrapItemHandlerCompat(capability);
					if (itemHandler == null)
						return null;

					int num = 0;
					int max = 0;
					for (int i = 0; i < itemHandler.getSlots(); i++) {
						ItemStack stackInSlot = itemHandler.getStackInSlot(i);
						if (StackHelper.isEmpty(stackInSlot)) {
							max += itemHandler.getSlotLimit(i);
						} else {
							max += itemHandler.getSlotLimit(i);
							num += (StackHelper.getStacksize(stackInSlot) * itemHandler.getSlotLimit(i)) / stackInSlot.getMaxStackSize();
						}

					}
					return new int[]{num, max};
				}
			});

			addWidget(new WidgetProgresSHorizScrollColored(Lang.translate("Contained Fluids"), x, y + y_dist, 0x4800FF) {
				@Override
				public int[] getData() {
					ItemStack stack = tile.stackSlot.getStack();
					if (StackHelper.isNull(stack)) return null;
					IItemFluidHandlerCompat fluidHandler = IItemFluidHandlerCompat.getFluidHandler(stack);
					if (fluidHandler == null) return null;
					int num = 0;
					int max = 0;
					for (IFluidTankProperties iFluidTankProperties : fluidHandler.getTankProperties()) {
						FluidStack contents = iFluidTankProperties.getContents();
						if (contents != null)
							num += contents.amount;
						max += iFluidTankProperties.getCapacity();
					}
					return new int[]{num, max};
				}
			});

			addWidget(new WidgetProgresSHorizScrollColored(Lang.translate("Contained Energy"), x, y + y_dist * 2, 0xFF3721) {
				@Override
				public int[] getData() {
					ItemStack stack = tile.stackSlot.getStack();
					if (StackHelper.isEmpty(stack))
						return null;
					IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
					if (energyStorage == null)
						return null;
					return new int[]{energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()};
				}
			});

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}

	public abstract static class WidgetProgresSHorizScrollColored extends WidgetBase implements IWidgetServerNetwork {
		final String text;
		final int color;
		int max = 0;
		int val = 0;

		public WidgetProgresSHorizScrollColored(String text, int x, int y, int color) {
			super(x, y, 52, 11);
			this.text = text;
			this.color = color;
		}

		@Override
		public List<String> getToolTip() {
			if (max == 0) {
				return ImmutableList.of(text);
			} else {
				return ImmutableList.of(text + ": " + StringHelper.format(val) + " / " + StringHelper.format(max));
			}
		}

		public abstract int[] getData();

		@Override
		public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 150, 52, 11);
			if (max > 0) {
				GlStateManager.color(ColorHelper.getRF(color), ColorHelper.getGF(color), ColorHelper.getBF(color), 1);
				gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, 0, 161, 50, 9);
				int displayValue = Math.round((val * 50F) / max);
				gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, 0, 170, displayValue, 9);
			} else {
				GlStateManager.color(ColorHelper.getRF(color) / 2, ColorHelper.getGF(color) / 2, ColorHelper.getBF(color) / 2, 1);
				gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, 0, 161, 50, 9);
			}
			GlStateManager.color(1, 1, 1, 1);
		}

		@Override
		public void addToDescription(XUPacketBuffer packet) {
			int[] data = getData();
			if (data == null) {
				packet.writeVarInt(0);
				packet.writeVarInt(0);
			} else {
				packet.writeVarInt(data[0]);
				packet.writeVarInt(data[1]);
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void handleDescriptionPacket(XUPacketBuffer packet) {
			val = packet.readVarInt();
			max = packet.readVarInt();
		}
	}
}
