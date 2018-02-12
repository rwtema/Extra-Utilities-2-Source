package com.rwtema.extrautils2.fluids;

import com.google.common.base.Throwables;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.NBTCopyHelper;
import java.io.IOException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidRef {

	private FluidRef() {

	}

	public static FluidRef wrap(FluidStack fluidStack) {
		if (fluidStack == null)
			return NullRef.NULL;

		if (fluidStack.tag != null) {
			return new FluidRef.NBT(fluidStack.getFluid(), fluidStack.tag);
		}

		return new FluidRef.Simple(fluidStack.getFluid());
	}

	public static FluidRef read(XUPacketBuffer buffer) {
		byte b = buffer.readByte();
		switch (b) {
			default:
			case 0:
				return NullRef.NULL;
			case 1:
				return Simple.readFromPacket(buffer);
			case 2:
				return NBT.readFromPacket(buffer);
		}

	}

	public abstract Fluid getFluid();

	public abstract NBTTagCompound getTagCompound();

	public abstract int getTagHash();

	public void write(XUPacketBuffer buffer) {
		writeToPacket(buffer);
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	protected abstract void writeToPacket(XUPacketBuffer buffer);

	public abstract boolean equalsFluidStack(FluidStack stack);

	public String getDisplayName() {
		FluidStack itemStack = createFluidStack(1);
		return itemStack.getLocalizedName();
	}

	protected abstract FluidStack createFluidStack(int i);

	private static class NullRef extends FluidRef {

		private static final NullRef NULL = new NullRef();

		@Override
		public Fluid getFluid() {
			return null;
		}

		@Override
		public NBTTagCompound getTagCompound() {
			return null;
		}

		@Override
		public int getTagHash() {
			return 0;
		}

		@Override
		protected void writeToPacket(XUPacketBuffer buffer) {
			buffer.writeByte(0);
		}

		@Override
		public String getDisplayName() {
			return "[Null]";
		}

		@Override
		public boolean equalsFluidStack(FluidStack stack) {
			return false;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		protected FluidStack createFluidStack(int i) {
			return null;
		}
	}

	private static class Simple extends FluidRef {
		private final Fluid fluid;

		public Simple(Fluid fluid) {
			this.fluid = fluid;
		}

		public static FluidRef readFromPacket(XUPacketBuffer buffer) {
			String s = buffer.readString();
			Fluid fluid = FluidRegistry.getFluid(s);
			if (fluid == null) return NullRef.NULL;
			return new FluidRef.Simple(fluid);
		}

		@Override
		public Fluid getFluid() {
			return fluid;
		}

		@Override
		public NBTTagCompound getTagCompound() {
			return null;
		}

		@Override
		public int getTagHash() {
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Simple that = (Simple) o;

			return fluid.equals(that.fluid);
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(fluid);
		}

		@Override
		protected void writeToPacket(XUPacketBuffer buffer) {
			buffer.writeByte(1);
			buffer.writeString(FluidRegistry.getFluidName(fluid));
		}

		@Override
		public boolean equalsFluidStack(FluidStack stack) {
			return stack.getFluid() == getFluid() && stack.tag == null;
		}

		@Override
		protected FluidStack createFluidStack(int i) {
			return new FluidStack(fluid, i, null);
		}


	}

	private static class NBT extends FluidRef {
		private final Fluid fluid;
		private final NBTTagCompound tag;
		private final int tagHash;
		byte[] packetBytes = null;

		public NBT(Fluid fluid, NBTTagCompound tag) {
			this.fluid = fluid;
			NBTCopyHelper.ResultNBT resultNBT = NBTCopyHelper.copyAndHashNBT(tag);
			this.tag = resultNBT.copy;
			this.tagHash = resultNBT.hash;
		}

		public NBT(Fluid fluid, NBTTagCompound tag, int tagHash) {
			this.fluid = fluid;
			this.tag = tag;
			this.tagHash = tagHash;
		}

		private static FluidRef readFromPacket(XUPacketBuffer buffer) {
			Fluid fluid = FluidRegistry.getFluid(buffer.readString());
			int tagHash = buffer.readInt();
			NBTTagCompound nbt = buffer.readNBT();
			if (fluid == null) return NullRef.NULL;
			return new NBT(fluid, nbt, tagHash);
		}

		@Override
		public Fluid getFluid() {
			return fluid;
		}

		@Override
		public NBTTagCompound getTagCompound() {
			return tag;
		}

		@Override
		public int getTagHash() {
			return tagHash;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			NBT that = (NBT) o;
			return fluid.equals(that.fluid) && tagHash == that.tagHash && tagEquals(that.tag);
		}

		public boolean tagEquals(NBTTagCompound otherTag) {
			return this.tag == otherTag || NBTCopyHelper.equalNBT(this.tag, otherTag);
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(fluid) * 31 + tagHash;
		}

		@Override
		protected void writeToPacket(XUPacketBuffer buffer) {
			buffer.writeByte(2);
			buffer.writeString(FluidRegistry.getFluidName(fluid));
			writeNBT(buffer);
		}

		private void writeNBT(XUPacketBuffer buffer) {
			buffer.writeInt(tagHash);

			if (packetBytes == null) {
				try {
					packetBytes = XUPacketBuffer.compress(tag);
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}

				if (packetBytes == null) {
					buffer.writeShort(0);
					return;
				}
			}

			buffer.writeShort(packetBytes.length);
			buffer.writeBytes(packetBytes);
		}

		@Override
		public boolean equalsFluidStack(FluidStack stack) {
			return false;
		}

		@Override
		protected FluidStack createFluidStack(int i) {
			return null;
		}
	}
}
