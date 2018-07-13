package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.fluids.FluidRef;
import com.rwtema.extrautils2.itemhandler.EmptyHandlerModifiable;
import com.rwtema.extrautils2.itemhandler.IItemHandlerModifiableCompat;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileProxy extends XUTile {
	public NBTSerializable.NBTObject<Vec3i, NBTTagIntArray> block_offset = registerNBT("block", new NBTSerializable.NBTObject<Vec3i, NBTTagIntArray>(BlockPos.ORIGIN) {

		@Override
		protected NBTTagIntArray serialize(Vec3i value) {
			return new NBTTagIntArray(new int[]{value.getX(), value.getY(), value.getZ()});
		}

		@Override
		protected Vec3i deserialize(NBTTagIntArray nbtTagIntArray) {
			int[] ints = nbtTagIntArray.getIntArray();
			return new Vec3i(ints[0], ints[1], ints[2]);
		}
	});

	ProxyFluid[] fluidProxies = null;
	ProxyItem[] itemProxies = null;

	@Override
	public void onLoad() {
		refreshProxies();
	}

	public void refreshProxies() {
		Vec3i value = block_offset.value;
		if (value.getX() == 0 && value.getY() == 0 && value.getZ() == 0) {
			fluidProxies = null;
			itemProxies = null;
		} else {
			fluidProxies = new ProxyFluid[7];
			itemProxies = new ProxyItem[7];
		}
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && fluidProxies != null
				|| capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemProxies != null;

	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
		int i = getSafeOrdinal(facing);

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			if (fluidProxies == null) return null;
			ProxyFluid fluidProxy = fluidProxies[i];
			if (fluidProxy == null) {
				fluidProxy = new ProxyFluid(this, pos.add(block_offset.value), facing);
				fluidProxies[i] = fluidProxy;
			}

			return (T) fluidProxy;
		}
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (itemProxies == null) return null;
			ProxyItem itemProxy = itemProxies[i];
			if (itemProxy == null) {
				itemProxy = new ProxyItem(this, pos.add(block_offset.value), facing);
				itemProxies[i] = itemProxy;
			}
			return (T) itemProxy;
		}

		return null;
	}

	@NetworkHandler.XUPacket
	public static class PacketProxyFlow extends XUPacketServerToClient {
		BlockPos start;
		BlockPos end;
		EnumFacing end_side;
		Object obj;


		@Override
		public void writeData() throws Exception {
			writeBlockPos(start);
			writeBlockPos(end);
			writeByte(getSafeOrdinal(end_side));
			if (obj instanceof ItemRef) {
				writeByte(0);
				((ItemRef) obj).write(this);
			} else if (obj instanceof FluidRef) {
				writeByte(1);
				((FluidRef) obj).write(this);
			}
			throw new RuntimeException("Invalid Object " + obj);
		}

		@Override
		public void readData(EntityPlayer player) {
			start = readBlockPos();
			end = readBlockPos();
			end_side = getSafeFacing(readByte());
			byte b = readByte();
			if (b == 0) {
				obj = ItemRef.read(this);
			} else if (b == 1) {
				obj = FluidRef.read(this);
			} else
				throw new RuntimeException("Invalid Object Type " + b);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new Runnable() {
				@Override
				public void run() {

				}
			};
		}
	}

	public static abstract class WrapperBase<C> {
		boolean lock = false;
		@Nonnull
		BlockPos pos;
		@Nonnull
		EnumFacing side;
		@Nonnull
		TileProxy proxy;

		public WrapperBase(@Nonnull TileProxy proxy, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
			this.pos = pos;
			this.side = side;
			this.proxy = proxy;
		}

		protected void sendParticle(Object obj) {

		}

		protected C getCap() {
			TileEntity tile = proxy.world.getTileEntity(pos);
			if (tile == null || tile.getClass() == TileProxy.class) return emptyCap();

			Capability<C> capability = getCapInstance();
			if (!tile.hasCapability(capability, side)) {
				return emptyCap();
			}
			C t = tile.getCapability(capability, side);
			if (t == null) return emptyCap();
			return t;
		}

		protected abstract Capability<C> getCapInstance();

		protected abstract C emptyCap();


		public boolean acquireLock() {
			if (lock) return false;
			lock = true;
			return true;
		}

		public void releaseLock() {
			lock = false;
		}
	}

	public static class ProxyFluid extends WrapperBase<IFluidHandler> implements IFluidHandler {
		public ProxyFluid(@Nonnull TileProxy proxy, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
			super(proxy, pos, side);
		}

		@Override
		protected Capability<IFluidHandler> getCapInstance() {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
		}

		@Override
		protected IFluidHandler emptyCap() {
			return EmptyFluidHandler.INSTANCE;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			if (!acquireLock()) return EmptyFluidHandler.EMPTY_TANK_PROPERTIES_ARRAY;
			IFluidHandler cap = getCap();
			IFluidTankProperties[] tankProperties = cap.getTankProperties();
			releaseLock();
			return tankProperties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (!acquireLock()) return 0;
			IFluidHandler cap = getCap();
			int fill = cap.fill(resource, doFill);
			releaseLock();
			return fill;

		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if (!acquireLock()) return null;
			IFluidHandler cap = getCap();
			FluidStack drain = cap.drain(resource, doDrain);
			releaseLock();
			return drain;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			if (!acquireLock()) return null;
			IFluidHandler cap = getCap();
			FluidStack drain = cap.drain(maxDrain, doDrain);
			releaseLock();
			return drain;
		}
	}


	public static class ProxyItem extends WrapperBase<IItemHandler> implements IItemHandlerModifiableCompat {

		public ProxyItem(@Nonnull TileProxy proxy, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
			super(proxy, pos, side);
		}

		@Override
		protected Capability<IItemHandler> getCapInstance() {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Override
		protected IItemHandler emptyCap() {
			return EmptyHandlerModifiable.INSTANCE;
		}

		@Override
		public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
			if (!acquireLock()) return;
			IItemHandler cap = getCap();
			if (cap instanceof IItemHandlerModifiable) {
				((IItemHandlerModifiable) cap).setStackInSlot(slot, stack);
			}
			releaseLock();
		}

		@Override
		public int getSlots() {
			if (!acquireLock()) return 0;
			IItemHandler cap = getCap();
			int slots = cap.getSlots();
			releaseLock();
			return slots;
		}

		@Override
		@ItemStackNonNull
		public ItemStack getStackInSlot(int slot) {
			if (!acquireLock()) return StackHelper.empty();
			IItemHandler cap = getCap();
			ItemStack stack = cap.getStackInSlot(slot);
			releaseLock();
			return stack;
		}

		@Override
		@ItemStackNonNull
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			if (StackHelper.isNull(stack) || !acquireLock()) return stack;
			IItemHandler cap = getCap();
			ItemStack insert = cap.insertItem(slot, stack, simulate);
			releaseLock();
			return insert;
		}

		@Override
		@ItemStackNonNull
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (amount == 0 || !acquireLock()) return StackHelper.empty();
			IItemHandler cap = getCap();
			ItemStack extract = cap.extractItem(slot, amount, simulate);
			releaseLock();
			return extract;
		}


	}


}
