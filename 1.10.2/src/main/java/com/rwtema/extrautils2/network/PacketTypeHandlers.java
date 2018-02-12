package com.rwtema.extrautils2.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketTypeHandlers {

	static HashMap<Class<?>, BiConsumer<XUPacketBuffer, Object>> writers = new HashMap<>();
	static HashMap<Class<?>, Function<XUPacketBuffer, Object>> readers = new HashMap<>();

	static {
		register(Integer.class, XUPacketBuffer::writeInt, XUPacketBuffer::readInt);
		register(Boolean.class, XUPacketBuffer::writeBoolean, XUPacketBuffer::readBoolean);
		register(String.class, XUPacketBuffer::writeString, XUPacketBuffer::readString);
		register(Short.class, (BiConsumer<XUPacketBuffer, Short>) XUPacketBuffer::writeShort, XUPacketBuffer::readShort);
		register(String.class, XUPacketBuffer::writeString, XUPacketBuffer::readString);
		register(BlockPos.class, XUPacketBuffer::writeBlockPos, XUPacketBuffer::readBlockPos);
		register(Double.class, XUPacketBuffer::writeDouble, XUPacketBuffer::readDouble);
		register(Long.class, XUPacketBuffer::writeLong, XUPacketBuffer::readLong);
		register(NBTTagCompound.class, XUPacketBuffer::writeNBT, XUPacketBuffer::readNBT);

	}

	public static <T> Class<T> ensureValid(Class<T> clazz) {
		if (writers.containsKey(clazz) && readers.containsKey(clazz)) {
			return clazz;
		}
		throw new IllegalArgumentException("Unsupported class: " + clazz);
	}

	public static <T> void register(Class<T> clazz, BiConsumer<XUPacketBuffer, T> writer, Function<XUPacketBuffer, T> reader) {
		writers.put(clazz, (BiConsumer) writer);
		readers.put(clazz, (Function) writer);
	}

}
