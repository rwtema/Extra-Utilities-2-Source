package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetClickMCButtonChoices;
import com.rwtema.extrautils2.itemhandler.IItemHandlerCompat;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class GrocketTransferFilter extends Grocket implements IDynamicHandler {
	public SingleStackHandlerFilter.EitherFilter filter = registerNBT("filter", new SingleStackHandlerFilter.EitherFilter());
	public NBTSerializable.NBTEnum<Limit> limit = registerNBT("limit", new NBTSerializable.NBTEnum<>(Limit.UNLIMITED));
	public NBTSerializable.NBTEnum<Order> order = registerNBT("order", new NBTSerializable.NBTEnum<>(Order.Normal));

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInterface(TileEntity tileEntity, CapGetter<T> capability) {
		if (capability == CapGetter.FluidHandler) {
			final IFluidHandler handler = CapGetter.FluidHandler.getInterface(tileEntity, side.getOpposite());
			if (handler != null) {
				return (T) new IFluidHandler() {

					@Override
					public IFluidTankProperties[] getTankProperties() {
						return handler.getTankProperties();
					}

					@Override
					public int fill(FluidStack resource, boolean doFill) {
						if (resource == null || !filter.matches(resource))
							return 0;
						return handler.fill(resource, doFill);
					}

					@Nullable
					@Override
					public FluidStack drain(FluidStack resource, boolean doDrain) {
						if (resource == null || !filter.matches(resource))
							return null;
						return handler.drain(resource, doDrain);
					}

					@Nullable
					@Override
					public FluidStack drain(int maxDrain, boolean doDrain) {
						if (maxDrain == 0)
							return null;
						FluidStack drain = handler.drain(maxDrain, false);
						if (drain == null || !filter.matches(drain))
							return null;
						if (doDrain)
							return handler.drain(maxDrain, true);
						else
							return drain;
					}
				};
			}
		}

		if (capability == CapGetter.ItemHandler) {
			final IItemHandler handler = CapGetter.ItemHandler.getInterface(tileEntity, side.getOpposite());
			if (handler != null)
				return (T) new IItemHandlerCompat() {


					@Override
					public int getSlots() {
						return handler.getSlots();
					}

					@ItemStackNonNull
					@Override
					public ItemStack getStackInSlot(int slot) {
						return handler.getStackInSlot(slot);
					}

					@ItemStackNonNull
					@Override
					public ItemStack insertItem(int slot, @ItemStackNonNull final ItemStack stack, boolean simulate) {
						if (StackHelper.isNull(stack)) return StackHelper.empty();
						if (!filter.matches(stack)) return stack;

						Limit limit = GrocketTransferFilter.this.limit.value;
						if (limit == Limit.UNLIMITED)
							return handler.insertItem(slot, stack, simulate);


						if (limit == Limit.SINGLE) {
							if (StackHelper.isNonNull(handler.getStackInSlot(slot))) {
								return stack;
							}
						} else {
							@Nullable
							ItemStack test = handler.insertItem(slot, stack, true);
							if (test == stack || (StackHelper.isNonNull(test) && StackHelper.getStacksize(test) == StackHelper.getStacksize(stack)))
								return stack;
						}

						for (int i = 0; i < handler.getSlots(); i++) {
							if (i == slot) continue;
							ItemStack stackInSlot = handler.getStackInSlot(i);
							if (StackHelper.isNonNull(stackInSlot) && ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
								return stack;
							}
						}

						if (limit == Limit.SINGLE && StackHelper.getStacksize(stack) > 1) {
							ItemStack singleVariant = ItemHandlerHelper.copyStackWithSize(stack, 1);
							boolean success = StackHelper.isNull(handler.insertItem(slot, singleVariant, simulate));
							if (success) {
								return ItemHandlerHelper.copyStackWithSize(stack, StackHelper.getStacksize(stack) - 1);
							} else
								return stack;
						}

						return handler.insertItem(slot, stack, simulate);
					}

					@ItemStackNonNull
					@Override
					public ItemStack extractItem(int slot, int amount, boolean simulate) {
						if (amount == 0) return StackHelper.empty();
						ItemStack itemStack = handler.extractItem(slot, amount, true);
						if (StackHelper.isNull(itemStack))
							return StackHelper.empty();
						if (!filter.matches(itemStack)) return StackHelper.empty();
						if (simulate)
							return itemStack;
						else
							return handler.extractItem(slot, amount, false);
					}
				};

		}

		return super.getInterface(tileEntity, capability);
	}

	@Override
	public GrocketType getType() {
		return GrocketType.FILTER_ITEMS;
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public boolean onActivated(EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!holder.getWorld().isRemote)
			holder.openGui(playerIn, this);
		return true;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new TransferNodeContainer(player);
	}

	enum Order {
		Normal,
		Reverse
	}

	enum Limit {
		UNLIMITED,
		SINGLE,
		SINGLESTACK,
	}

	public class TransferNodeContainer extends DynamicContainerTile {
		public TransferNodeContainer(EntityPlayer player) {
			super(holder);
			addTitle("Transfer Filter");
			crop();
			addWidget(filter.newSlot(4, height + 4));
			addWidget(new WidgetClickMCButtonChoices<Limit>(4 + 18 + 4, height + 4) {
						@Override
						protected void onSelectedServer(Limit marker) {
							limit.value = marker;
							markDirty();
						}

						@Override
						public Limit getSelectedValue() {
							return limit.value;
						}
					}
							.addChoice(Limit.UNLIMITED, "Unlimited", null)
							.addChoice(Limit.SINGLE, "Single Item", null)
							.addChoice(Limit.SINGLESTACK, "Single Stack", null)
			);

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
