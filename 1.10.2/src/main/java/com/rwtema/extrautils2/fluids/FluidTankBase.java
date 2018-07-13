package com.rwtema.extrautils2.fluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public abstract class FluidTankBase implements IFluidTank, net.minecraftforge.fluids.capability.IFluidHandler {

	protected TileEntity tile;
	protected boolean canFill = true;
	protected boolean canDrain = true;
	protected IFluidTankProperties[] tankProperties;

	public FluidTankBase readFromNBT(NBTTagCompound nbt) {
		if (!nbt.hasKey("Empty")) {
			FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
			setFluid(fluid);
		} else {
			setFluid(null);
		}
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (getFluid() != null) {
			getFluid().writeToNBT(nbt);
		} else {
			nbt.setString("Empty", "");
		}
		return nbt;
	}

	/* IFluidTank */
	@Override
	@Nullable
	public abstract FluidStack getFluid();

	public abstract void setFluid(@Nullable FluidStack fluid);

	@Override
	public int getFluidAmount() {
		if (getFluid() == null) {
			return 0;
		}
		return getFluid().amount;
	}

	@Override
	public abstract int getCapacity();

	public void setTileEntity(TileEntity tile) {
		this.tile = tile;
	}

	@Override
	public FluidTankInfo getInfo() {
		return new FluidTankInfo(this);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		if (this.tankProperties == null) {
			this.tankProperties = new IFluidTankProperties[]{new IFluidTankProperties() {
				@Nullable
				@Override
				public FluidStack getContents() {
					FluidStack fluid = getFluid();
					return fluid != null ? fluid.copy() : null;
				}

				@Override
				public int getCapacity() {
					return FluidTankBase.this.getCapacity();
				}

				@Override
				public boolean canFill() {
					return FluidTankBase.this.canFill();
				}

				@Override
				public boolean canDrain() {
					return FluidTankBase.this.canDrain();
				}

				@Override
				public boolean canFillFluidType(FluidStack fluidStack) {
					return FluidTankBase.this.canFillFluidType(fluidStack);
				}

				@Override
				public boolean canDrainFluidType(FluidStack fluidStack) {
					return FluidTankBase.this.canDrainFluidType(fluidStack);
				}
			}};
		}
		return this.tankProperties;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (!canFillFluidType(resource)) {
			return 0;
		}

		return fillInternal(resource, doFill);
	}

	/**
	 * Use this method to bypass the restrictions from {@link #canFillFluidType(FluidStack)}
	 * Meant for use by the owner of the tank when they have {@link #canFill() set to false}.
	 */
	public int fillInternal(FluidStack resource, boolean doFill) {
		if (resource == null || resource.amount <= 0) {
			return 0;
		}

		FluidStack fluid = getFluid();
		if (!doFill) {
			if (fluid == null) {
				return Math.min(getCapacity(), resource.amount);
			}

			if (!fluid.isFluidEqual(resource)) {
				return 0;
			}

			return Math.min(getCapacity() - fluid.amount, resource.amount);
		}

		if (fluid == null) {
			setFluid(fluid = new FluidStack(resource, Math.min(getCapacity(), resource.amount)));

			onContentsChanged();

			if (tile != null) {
				FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, fluid.amount));
			}
			return fluid.amount;
		}

		if (!fluid.isFluidEqual(resource)) {
			return 0;
		}

		fluid = fluid.copy();

		int filled = getCapacity() - fluid.amount;

		if (resource.amount < filled) {
			fluid.amount += resource.amount;
			filled = resource.amount;
		} else {
			fluid.amount = getCapacity();
		}

		onContentsChanged();

		setFluid(fluid);

		if (tile != null) {
			FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, filled));
		}
		return filled;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (!canDrainFluidType(getFluid())) {
			return null;
		}
		return drainInternal(resource, doDrain);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (!canDrainFluidType(getFluid())) {
			return null;
		}
		return drainInternal(maxDrain, doDrain);
	}

	/**
	 * Use this method to bypass the restrictions from {@link #canDrainFluidType(FluidStack)}
	 * Meant for use by the owner of the tank when they have {@link #canDrain()} set to false}.
	 */
	public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
		if (resource == null || !resource.isFluidEqual(getFluid())) {
			return null;
		}
		return drainInternal(resource.amount, doDrain);
	}

	/**
	 * Use this method to bypass the restrictions from {@link #canDrainFluidType(FluidStack)}
	 * Meant for use by the owner of the tank when they have {@link #canDrain()} set to false}.
	 */
	public FluidStack drainInternal(int maxDrain, boolean doDrain) {
		FluidStack fluid = getFluid();
		if (fluid == null || maxDrain <= 0) {
			return null;
		}

		int drained = maxDrain;
		if (fluid.amount < drained) {
			drained = fluid.amount;
		}

		FluidStack stack = new FluidStack(fluid, drained);
		if (doDrain) {
			fluid = fluid.copy();
			fluid.amount -= drained;
			if (fluid.amount <= 0) {
				setFluid(fluid = null);
			} else {
				setFluid(fluid);
			}

			onContentsChanged();

			if (tile != null) {
				FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, tile.getWorld(), tile.getPos(), this, drained));
			}
		}
		return stack;
	}

	/**
	 * Whether this tank can be filled with {@link net.minecraftforge.fluids.capability.IFluidHandler}
	 *
	 * @see IFluidTankProperties#canFill()
	 */
	public boolean canFill() {
		return canFill;
	}

	/**
	 * Whether this tank can be drained with {@link net.minecraftforge.fluids.capability.IFluidHandler}
	 *
	 * @see IFluidTankProperties#canDrain()
	 */
	public boolean canDrain() {
		return canDrain;
	}

	/**
	 * Set whether this tank can be filled with {@link net.minecraftforge.fluids.capability.IFluidHandler}
	 *
	 * @see IFluidTankProperties#canFill()
	 */
	public void setCanFill(boolean canFill) {
		this.canFill = canFill;
	}

	/**
	 * Set whether this tank can be drained with {@link net.minecraftforge.fluids.capability.IFluidHandler}
	 *
	 * @see IFluidTankProperties#canDrain()
	 */
	public void setCanDrain(boolean canDrain) {
		this.canDrain = canDrain;
	}

	/**
	 * Returns true if the tank can be filled with this type of fluidName.
	 * Used as x filter for fluidName types.
	 * Does not consider the current contents or capacity of the tank,
	 * only whether it could ever fill with this type of fluidName.
	 *
	 * @see IFluidTankProperties#canFillFluidType(FluidStack)
	 */
	public boolean canFillFluidType(FluidStack fluid) {
		return canFill();
	}

	/**
	 * Returns true if the tank can drain out this type of fluidName.
	 * Used as x filter for fluidName types.
	 * Does not consider the current contents or capacity of the tank,
	 * only whether it could ever drain out this type of fluidName.
	 *
	 * @see IFluidTankProperties#canDrainFluidType(FluidStack)
	 */
	public boolean canDrainFluidType(FluidStack fluid) {
		return canDrain();
	}

	protected void onContentsChanged() {

	}
}
