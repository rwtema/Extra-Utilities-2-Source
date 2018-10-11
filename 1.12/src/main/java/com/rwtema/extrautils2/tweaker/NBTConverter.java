package com.rwtema.extrautils2.tweaker;

import crafttweaker.api.data.*;
import net.minecraft.nbt.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Stan
 */
public class NBTConverter implements IDataConverter<NBTBase> {

	public static final NBTConverter INSTANCE = new NBTConverter();

	public static NBTBase from(IData data) {
		return data.convert(INSTANCE);
	}

	@Override
	public NBTBase fromBool(boolean value) {
		return new NBTTagInt(value ? 1 : 0);
	}

	@Override
	public NBTBase fromByte(byte value) {
		return new NBTTagByte(value);
	}

	@Override
	public NBTBase fromShort(short value) {
		return new NBTTagShort(value);
	}

	@Override
	public NBTBase fromInt(int value) {
		return new NBTTagInt(value);
	}

	@Override
	public NBTBase fromLong(long value) {
		return new NBTTagLong(value);
	}

	@Override
	public NBTBase fromFloat(float value) {
		return new NBTTagFloat(value);
	}

	@Override
	public NBTBase fromDouble(double value) {
		return new NBTTagDouble(value);
	}

	@Override
	public NBTBase fromString(String value) {
		return new NBTTagString(value);
	}

	@Override
	public NBTBase fromList(List<IData> values) {
		NBTTagList result = new NBTTagList();
		for(IData value : values) {
			result.appendTag(from(value));
		}
		return result;
	}

	@Override
	public NBTBase fromMap(Map<String, IData> values) {
		NBTTagCompound result = new NBTTagCompound();
		for(Map.Entry<String, IData> entry : values.entrySet()) {
			result.setTag(entry.getKey(), from(entry.getValue()));
		}
		return result;
	}

	@Override
	public NBTBase fromByteArray(byte[] value) {
		return new NBTTagByteArray(value);
	}

	@Override
	public NBTBase fromIntArray(int[] value) {
		return new NBTTagIntArray(value);
	}
}
