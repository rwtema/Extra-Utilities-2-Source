package com.rwtema.extrautils2.network;

import com.google.common.base.Throwables;
import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.compatibility.StackHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.UUID;

public class XUPacketBuffer {
	public ByteBuf data;

	public XUPacketBuffer() {
		data = Unpooled.buffer();
	}

	public XUPacketBuffer(ByteBuf data) {
		this.data = data;
	}

	public static byte[] compress(NBTTagCompound p_74798_0_) throws IOException {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		CompressedStreamTools.writeCompressed(p_74798_0_, bytearrayoutputstream);
		return bytearrayoutputstream.toByteArray();
	}

	public static NBTTagCompound readCompressed(byte[] p_152457_0_) throws IOException {
		return CompressedStreamTools.readCompressed(new ByteArrayInputStream(p_152457_0_));
	}

	public ByteBuf writeBoolean(boolean value) {
		return data.writeBoolean(value);
	}

	public ByteBuf writeByte(int value) {
		return data.writeByte(value);
	}

	public ByteBuf writeShort(int value) {
		return data.writeShort(value);
	}

	public ByteBuf writeMedium(int value) {
		return data.writeMedium(value);
	}

	public ByteBuf writeInt(int value) {
		return data.writeInt(value);
	}

	public ByteBuf writeLong(long value) {
		return data.writeLong(value);
	}

	public ByteBuf writeChar(int value) {
		return data.writeChar(value);
	}

	public ByteBuf writeFloat(float value) {
		return data.writeFloat(value);
	}

	public ByteBuf writeDouble(double value) {
		return data.writeDouble(value);
	}

	public ByteBuf writeBytes(ByteBuf src) {
		return data.writeBytes(src);
	}

	public ByteBuf writeBytes(ByteBuf src, int length) {
		return data.writeBytes(src, length);
	}

	public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
		return data.writeBytes(src, srcIndex, length);
	}

	public ByteBuf writeBytes(byte[] src) {
		return data.writeBytes(src);
	}

	public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
		return data.writeBytes(src, srcIndex, length);
	}

	public ByteBuf writeBytes(ByteBuffer src) {
		return data.writeBytes(src);
	}

	public int writeBytes(InputStream in, int length) throws IOException {
		return data.writeBytes(in, length);
	}

	public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
		return data.writeBytes(in, length);
	}

	public ByteBuf writeZero(int length) {
		return data.writeZero(length);
	}

	public boolean readBoolean() {
		return data.readBoolean();
	}

	public byte readByte() {
		return data.readByte();
	}

	public short readUnsignedByte() {
		return data.readUnsignedByte();
	}

	public short readShort() {
		return data.readShort();
	}

	public int readUnsignedShort() {
		return data.readUnsignedShort();
	}

	public int readMedium() {
		return data.readMedium();
	}

	public int readUnsignedMedium() {
		return data.readUnsignedMedium();
	}

	public int readInt() {
		return data.readInt();
	}

	public long readUnsignedInt() {
		return data.readUnsignedInt();
	}

	public long readLong() {
		return data.readLong();
	}

	public char readChar() {
		return data.readChar();
	}

	public float readFloat() {
		return data.readFloat();
	}

	public double readDouble() {
		return data.readDouble();
	}

	public ByteBuf readBytes(int length) {
		return data.readBytes(length);
	}

	public void writeVec(Vec3d vec3) {
		data.writeFloat((float) vec3.x);
		data.writeFloat((float) vec3.y);
		data.writeFloat((float) vec3.z);
	}

	public void writeChatComponent(ITextComponent chatComponent) {
		writeString(ITextComponent.Serializer.componentToJson(chatComponent));
	}

	public ITextComponent readChatComponent() {
		return ITextComponent.Serializer.jsonToComponent(readString());
	}

	public Vec3d readVec() {
		return new Vec3d(data.readFloat(), data.readFloat(), data.readFloat());
	}

	public void writeSmallString(String string) {
		if (string == null) {
			data.writeByte(0);
		} else {
			byte[] stringData = string.getBytes(Charset.forName("UTF-8"));
			data.writeByte(stringData.length);
			data.writeBytes(stringData);
		}
	}


	public String readSmallString() {
		short length = data.readUnsignedByte();
		if (length == 0) return "";
		byte[] bytes = new byte[length];

		data.readBytes(bytes);
		return new String(bytes, Charset.forName("UTF-8"));
	}


	public void writeString(String string) {
		if (string == null) {
			data.writeShort(0);
		} else {
			byte[] stringData = string.getBytes(Charset.forName("UTF-8"));
			data.writeShort(stringData.length);
			data.writeBytes(stringData);
		}
	}

	public String readString() {
		short length = data.readShort();
		if (length == 0) return "";
		byte[] bytes = new byte[length];

		data.readBytes(bytes);
		return new String(bytes, Charset.forName("UTF-8"));
	}

	public void writeNBT(NBTTagCompound tag) {
		if (tag == null) {
			data.writeShort(0);
			return;
		}
		try {
			byte[] compressed = compress(tag);

			data.writeShort(compressed.length);
			data.writeBytes(compressed);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Nullable
	public NBTTagCompound readNBT() {
		try {
			return readNBTChecked();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Nullable
	public NBTTagCompound readNBTChecked() throws IOException {
		int length = data.readUnsignedShort();
		if (length <= 0) return null;
		byte[] bytes = new byte[length];
		data.readBytes(bytes);
		return readCompressed(bytes);
	}

	public void writeItemStack(ItemStack item) {
		if (StackHelper.isNull(item) || item.getItem() == StackHelper.nullItem()) {
			data.writeByte(0);
		} else {
			data.writeByte(StackHelper.getStacksize(item));
			data.writeShort(Item.getIdFromItem(item.getItem()));
			data.writeShort(item.getItemDamage());

			NBTTagCompound nbttagcompound = null;

			if (item.getItem().getShareTag() || item.getItem().isDamageable()) {
				nbttagcompound = item.getTagCompound();
			}

			writeNBT(nbttagcompound);
		}
	}

	public ItemStack readItemStack() {
		ItemStack itemstack;
		short stackSize = data.readUnsignedByte();

		if (stackSize == 0) return StackHelper.empty();

		short id = data.readShort();
		short metadata = data.readShort();

		Item itemById = Item.getItemById(id);
		if (itemById == StackHelper.nullItem()) return StackHelper.empty();
		itemstack = new ItemStack(itemById, stackSize, metadata);
		itemstack.setTagCompound(readNBT());

		return itemstack;
	}

	public void writeBlockPos(BlockPos pos) {
		data.writeInt(pos.getX());
		data.writeByte(pos.getY());
		data.writeInt(pos.getZ());
	}

	public BlockPos readBlockPos() {
		return new BlockPos(data.readInt(), data.readUnsignedByte(), data.readInt());
	}

	public void writeSide(EnumFacing e) {
		data.writeByte(e.ordinal());
	}

	public EnumFacing readSide() {
		return EnumFacing.values()[data.readByte()];
	}

	public void writePacketBuffer(XUPacketBuffer packetBuffer) {
		writeInt(packetBuffer.data.readableBytes());
		data.writeBytes(packetBuffer.data);
	}

	public XUPacketBuffer readPacketBuffer() {
		int len = readInt();
		ByteBuf buffer = Unpooled.buffer(len);
		data.readBytes(buffer, len);
		return new XUPacketBuffer(buffer);
	}

	public GameProfile readProfile() {
		byte b = readByte();
		if (b == 0) return null;
		String name;
		if ((b & 1) != 0) {
			name = readString();
		} else
			name = null;

		UUID id;
		if ((b & 2) != 0) {
			long low = readLong();
			long upp = readLong();
			id = new UUID(upp, low);
		} else
			id = null;

		return new GameProfile(id, name);
	}


	public void writeProfile(GameProfile profile) {
		UUID id = profile.getId();
		String name = profile.getName();
		if (id == null) {
			if (StringUtils.isBlank(name)) {
				writeByte(0);
			} else {
				writeByte(1);
				writeString(name);
			}
		} else {
			long low = id.getLeastSignificantBits();
			long upp = id.getMostSignificantBits();
			if (StringUtils.isBlank(name)) {
				writeByte(2);
				writeLong(low);
				writeLong(upp);
			} else {
				writeByte(3);
				writeString(name);
				writeLong(low);
				writeLong(upp);
			}
		}
	}

	public void writeFluidStack(FluidStack fluid) {
		if (fluid == null || fluid.getFluid() == null) {
			writeString(null);
		} else {
			writeString(FluidRegistry.getFluidName(fluid));
			writeInt(fluid.amount);
			writeNBT(fluid.tag);
		}
	}

	public FluidStack readFluidStack() {
		String s = readString();
		if (s == null || "".equals(s)) return null;
		Fluid fluid = FluidRegistry.getFluid(s);
		int amount = readInt();
		NBTTagCompound tag = readNBT();
		return new FluidStack(fluid, amount, tag);
	}

	public <T> void writeType(Class<T> clazz, T object) {
		writeTypeUnchecked(clazz, object);
	}

	public void writeTypeUnchecked(Class<?> clazz, Object object) {
		Validate.notNull(PacketTypeHandlers.writers.get(clazz)).accept(this, object);
	}

	@SuppressWarnings("unchecked")
	public <T> T readType(Class<T> clazz) {
		return (T) readTypeUnchecked(clazz);
	}

	public <T> Object readTypeUnchecked(Class<T> clazz) {
		return Validate.notNull(PacketTypeHandlers.readers.get(clazz)).apply(this);
	}


	public int readVarInt() {
		int i = 0;
		int j = 0;

		while (true) {
			byte b0 = this.readByte();
			i |= (b0 & 127) << j++ * 7;

			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}

			if ((b0 & 128) != 128) {
				break;
			}
		}

		return i;
	}

	public void writeVarInt(int input) {
		while ((input & -128) != 0) {
			this.writeByte(input & 127 | 128);
			input >>>= 7;
		}

		this.writeByte(input);
	}

}
