package com.rwtema.extrautils2.utils.datastructures;


import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class NBTSerializable {
	public static NBTPrimitive getLowestNeeded(int value) {
		byte byteVal = (byte) value;
		if (value == (int) byteVal) {
			return new NBTTagByte(byteVal);
		}

		short shortVal = (short) value;
		if (value == (int) shortVal) {
			return new NBTTagShort(shortVal);
		}

		return new NBTTagInt(value);
	}

	public static NBTPrimitive getLowestNeeded(long value) {
		byte byteVal = (byte) value;
		if (value == (long) byteVal) {
			return new NBTTagByte(byteVal);
		}

		short shortVal = (short) value;
		if (value == (long) shortVal) {
			return new NBTTagShort(shortVal);
		}

		int intVal = (int) value;
		if (value == (long) intVal) {
			return new NBTTagInt(intVal);
		}

		return new NBTTagLong(value);
	}

	public static class Text implements INBTSerializable<NBTTagString> {
		@Nonnull
		String s;

		public Text(@Nonnull String s) {
			this.s = s;
		}

		@Nonnull
		public static NBTTagString serialize(String s) {
			return new NBTTagString(s);
		}

		@Override
		public NBTTagString serializeNBT() {
			return serialize(s);
		}

		@Override
		public void deserializeNBT(NBTTagString nbt) {
			s = deserialize(nbt);
		}

		@Nonnull
		public String deserialize(NBTTagString nbt) {
			return nbt.getString();
		}
	}

	public static class Long implements INBTSerializable<NBTTagLong> {
		public long value;

		@Override
		public NBTTagLong serializeNBT() {
			return new NBTTagLong(value);
		}

		@Override
		public void deserializeNBT(NBTTagLong nbt) {
			value = nbt.getLong();
		}
	}

	public static class BitsSmall implements INBTSerializable<NBTTagByte>, ISimpleBitSet {
		byte bitSet;

		public BitsSmall(byte b) {
			bitSet = b;
		}

		@Override
		public NBTTagByte serializeNBT() {
			return new NBTTagByte(bitSet);
		}

		@Override
		public void deserializeNBT(NBTTagByte nbt) {
			bitSet = nbt.getByte();
		}

		@Override
		public boolean get(int bitIndex) {
			return (bitSet & (1L << bitIndex)) != 0;
		}

		@Override
		public void flip(int bitIndex) {
			bitSet ^= (1L << bitIndex);
		}

		@Override
		public void set(int bitIndex) {
			bitSet |= (1L << bitIndex);
		}

		@Override
		public void set(int bitIndex, boolean value) {
			if (value)
				set(bitIndex);
			else
				clear(bitIndex);
		}

		@Override
		public void clear(int bitIndex) {
			bitSet &= ~(1L << bitIndex);
		}

		@Override
		public boolean isEmpty() {
			return bitSet != 0;
		}
	}


	public static class BitsLong implements INBTSerializable<NBTPrimitive>, ISimpleBitSet {
		long bitSet;

		@Override
		public NBTPrimitive serializeNBT() {
			return getLowestNeeded(bitSet);
		}

		@Override
		public void deserializeNBT(NBTPrimitive nbt) {
			bitSet = nbt.getLong();
		}

		@Override
		public boolean get(int bitIndex) {
			return (bitSet & (1L << bitIndex)) != 0;
		}

		@Override
		public void flip(int bitIndex) {
			bitSet ^= (1L << bitIndex);
		}

		@Override
		public void set(int bitIndex) {
			bitSet |= (1L << bitIndex);
		}

		@Override
		public void set(int bitIndex, boolean value) {
			if (value)
				set(bitIndex);
			else
				clear(bitIndex);
		}

		@Override
		public void clear(int bitIndex) {
			bitSet &= ~(1L << bitIndex);
		}

		@Override
		public boolean isEmpty() {
			return bitSet != 0;
		}
	}

	public static class Int implements INBTSerializable<NBTTagInt> {
		public int value;

		public Int(int value) {
			this.value = value;
		}

		public Int() {
			super();
		}

		@Nonnull
		public static NBTTagInt serialize(int value) {
			return new NBTTagInt(value);
		}

		public static int deserialize(NBTTagInt nbt) {
			return nbt.getInt();
		}

		@Override
		public NBTTagInt serializeNBT() {
			return serialize(value);
		}

		@Override
		public void deserializeNBT(NBTTagInt nbt) {
			value = deserialize(nbt);
		}
	}

	public static class Float implements INBTSerializable<NBTTagFloat> {
		public float value;

		public Float() {
			super();
		}

		public Float(float value) {
			this.value = value;
		}

		@Override
		public NBTTagFloat serializeNBT() {
			return new NBTTagFloat(value);
		}

		@Override
		public void deserializeNBT(NBTTagFloat nbt) {
			value = nbt.getFloat();
		}
	}

	public static class NBTDouble implements INBTSerializable<NBTTagDouble> {
		public double value;

		public NBTDouble(double value) {
			this.value = value;
		}

		@Override
		public NBTTagDouble serializeNBT() {
			return new NBTTagDouble(value);
		}

		@Override
		public void deserializeNBT(NBTTagDouble nbt) {
			value = nbt.getDouble();
		}
	}

	public static class NBTBoolean implements INBTSerializable<NBTTagByte> {
		public boolean value;

		public NBTBoolean() {
			this(false);
		}

		public NBTBoolean(boolean value) {
			this.value = value;
		}

		@Override
		public NBTTagByte serializeNBT() {
			return new NBTTagByte(value ? (byte) 1 : 0);
		}

		@Override
		public void deserializeNBT(NBTTagByte nbt) {
			value = nbt.getByte() != 0;
		}
	}

	public static class NBTEnumNullable<T extends Enum<T>> implements INBTSerializable<NBTTagShort> {
		private final Class<T> clazz;
		public T value;

		public NBTEnumNullable(Class<T> clazz) {
			this.value = null;
			this.clazz = clazz;
		}

		@Override
		public NBTTagShort serializeNBT() {
			return new NBTTagShort(value == null ? -1 : (short) value.ordinal());
		}

		@Override
		public void deserializeNBT(NBTTagShort nbt) {
			T[] enumConstants = clazz.getEnumConstants();
			short i = nbt.getShort();
			if (i == -1)
				value = null;
			else
				value = enumConstants[i];
		}
	}

	public static class NBTEnum<T extends Enum<T>> implements INBTSerializable<NBTTagShort> {
		@Nonnull
		public T value;

		public NBTEnum(@Nonnull T value) {
			this.value = value;
		}

		@Override
		public NBTTagShort serializeNBT() {
			return new NBTTagShort((short) value.ordinal());
		}

		@Override
		public void deserializeNBT(NBTTagShort nbt) {
			Class<T> aClass = value.getDeclaringClass();
			T[] enumConstants = aClass.getEnumConstants();
			short i = nbt.getShort();
			if (enumConstants == null)
				throw new NullPointerException();
			value = enumConstants[i];
		}
	}

	public static class NBTStack implements INBTSerializable<NBTBase> {
		ItemStack value = StackHelper.empty();

		public void setStackRaw(ItemStack other) {
			value = other;
		}

		public void setStackCopy(ItemStack other) {
			value = StackHelper.safeCopy(other);
		}

		@Override
		public NBTBase serializeNBT() {
			if (StackHelper.isNull(value)) {
				return new NBTTagByte((byte) 0);
			}

			return value.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			value = nbt.getId() == Constants.NBT.TAG_COMPOUND ? StackHelper.loadFromNBT((NBTTagCompound) nbt) : StackHelper.empty();
		}

		public ItemStack getRaw() {
			return value;
		}

		public ItemStack getCopy() {
			return StackHelper.safeCopy(value);
		}

		public ItemStack getCopy(int amount) {
			return ItemHandlerHelper.copyStackWithSize(value, amount);
		}

		public boolean isNull() {
			return StackHelper.isNull(value);
		}
	}

	public abstract static class NBTNullable<T extends INBTSerializable<K>, K extends NBTBase> implements INBTSerializable<NBTBase> {
		T value;

		@Override
		public NBTBase serializeNBT() {
			if (value == null) {
				return new NBTTagByte((byte) 0);
			}

			return value.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			if (nbt.getId() == Constants.NBT.TAG_BYTE) {
				value = null;
			} else {
				value = newBlankValue();
				value.deserializeNBT((K) nbt);
			}
		}

		public abstract T newBlankValue();
	}

	public static class MapSerializable<K, V, K_NBT extends NBTBase, V_NBT extends NBTBase> implements INBTSerializable<NBTTagList> {
		private final Function<V, V_NBT> val_serializer;
		private final Function<V_NBT, V> val_deserializer;
		private final Function<K, K_NBT> key_serializer;
		private final Function<K_NBT, K> key_deserializer;
		public Map<K, V> map;

		public MapSerializable(Map<K, V> map, Function<K, K_NBT> key_serializer, Function<K_NBT, K> key_deserializer, Function<V, V_NBT> val_serializer, Function<V_NBT, V> val_deserializer) {
			this.map = map;
			this.val_serializer = val_serializer;
			this.val_deserializer = val_deserializer;
			this.key_serializer = key_serializer;
			this.key_deserializer = key_deserializer;
		}

		@Override
		public NBTTagList serializeNBT() {
			NBTTagList list = new NBTTagList();
			for (Map.Entry<K, V> entry : map.entrySet()) {
				if (entry.getKey() == null || entry.getValue() == null) continue;
				K_NBT k = key_serializer.apply(entry.getKey());
				V_NBT v = val_serializer.apply(entry.getValue());
				if (k == null || v == null) continue;
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setTag("k", k);
				nbt.setTag("v", v);
				list.appendTag(nbt);
			}
			return list;
		}

		@Override
		public void deserializeNBT(NBTTagList nbt) {
			map.clear();
			for (NBTTagCompound compound : NBTHelper.iterateNBTTagList(nbt)) {
				K k = key_deserializer.apply((K_NBT) compound.getTag("k"));
				V v = val_deserializer.apply((V_NBT) compound.getTag("v"));
				if (v != null) {
					map.put(k, v);
				}
			}
		}
	}

	public static class HashMapSerializable<K, V, V_NBT extends NBTBase> implements INBTSerializable<NBTTagCompound> {
		public final HashMap<K, V> map = new HashMap<>();
		private final Function<V, V_NBT> val_serializer;
		private final Function<V_NBT, V> val_deserializer;
		private final Function<K, String> key_serializer;
		private final Function<String, K> key_deserializer;

		public HashMapSerializable(Function<K, String> key_serializer, Function<String, K> key_deserializer, Function<V, V_NBT> val_serializer, Function<V_NBT, V> val_deserializer) {
			this.val_serializer = val_serializer;
			this.val_deserializer = val_deserializer;
			this.key_serializer = key_serializer;
			this.key_deserializer = key_deserializer;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			for (Map.Entry<K, V> entry : map.entrySet()) {
				nbt.setTag(key_serializer.apply(entry.getKey()), val_serializer.apply(entry.getValue()));
			}
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			map.clear();
			for (String s : nbt.getKeySet()) {
				map.put(key_deserializer.apply(s), val_deserializer.apply((V_NBT) nbt.getTag(s)));

			}
		}
	}

	public static class NBTResourceLocationSerializable implements INBTSerializable<NBTTagString> {
		ResourceLocation location;

		public NBTResourceLocationSerializable(ResourceLocation location) {
			this.location = location;
		}

		@Nonnull
		public static NBTTagString serialize(ResourceLocation location) {
			return location != null ? new NBTTagString(location.toString()) : new NBTTagString();
		}

		@javax.annotation.Nullable
		public static ResourceLocation deserialize(NBTTagString nbt) {
			String s = nbt.getString();
			return !s.isEmpty() ? new ResourceLocation(s) : null;
		}

		@Override
		public NBTTagString serializeNBT() {
			return serialize(location);

		}

		@Override
		public void deserializeNBT(NBTTagString nbt) {
			location = deserialize(nbt);
		}
	}

	public static class NBTByteArray implements INBTSerializable<NBTTagByteArray> {
		@Nonnull
		public byte[] array;

		public NBTByteArray(@Nonnull byte[] array) {
			this.array = array;
		}

		@Override
		public NBTTagByteArray serializeNBT() {
			return new NBTTagByteArray(array);
		}

		@Override
		public void deserializeNBT(NBTTagByteArray nbt) {
			array = nbt.getByteArray();
		}
	}

	public static class NBTIntArray implements INBTSerializable<NBTTagIntArray> {
		@Nonnull
		public int[] array;

		public NBTIntArray(@Nonnull int[] array) {
			this.array = array;
		}


		@Override
		public NBTTagIntArray serializeNBT() {
			return new NBTTagIntArray(array);
		}

		@Override
		public void deserializeNBT(NBTTagIntArray nbt) {
			array = nbt.getIntArray();
		}
	}

	public static class NBTImmutableListSerializable<V extends INBTSerializable<V_NBT>, V_NBT extends NBTBase> implements INBTSerializable<NBTTagList> {
		final ImmutableList<V> list;

		public NBTImmutableListSerializable(ImmutableList<V> list) {
			this.list = list;
		}

		@Override
		public NBTTagList serializeNBT() {
			NBTTagList nbtTagList = new NBTTagList();
			for (V v : list) {
				nbtTagList.appendTag(v.serializeNBT());
			}
			return null;
		}

		@Override
		public void deserializeNBT(NBTTagList nbt) {
			for (int i = 0; i < list.size(); i++) {
				list.get(i).deserializeNBT((V_NBT) nbt.get(i));
			}
		}
	}

	public static class NBTArrayListSerializable<V, V_NBT extends NBTBase> implements INBTSerializable<NBTTagList> {
		public final ArrayList<V> list = new ArrayList<>();
		private final Function<V, V_NBT> serializer;
		private final Function<V_NBT, V> deserializer;

		public NBTArrayListSerializable(Function<V, V_NBT> serializer, Function<V_NBT, V> deserializer) {
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		@Override
		public NBTTagList serializeNBT() {
			NBTTagList nbtTagList = new NBTTagList();
			for (V v : list) {
				nbtTagList.appendTag(serializer.apply(v));
			}
			return nbtTagList;
		}

		@Override
		public void deserializeNBT(NBTTagList nbt) {
			list.clear();
			for (int i = 0; i < nbt.tagCount(); i++) {
				list.add(deserializer.apply((V_NBT) nbt.get(i)));
			}
		}
	}

	public static class Vec implements INBTSerializable<NBTTagCompound> {
		public double x, y, z;

		public void set(Vec3d vec) {
			x = vec.x;
			y = vec.y;
			z = vec.z;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setDouble("x", x);
			tag.setDouble("y", y);
			tag.setDouble("z", z);
			return tag;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			x = nbt.getDouble("x");
			y = nbt.getDouble("y");
			z = nbt.getDouble("z");
		}

		public void writeToPacket(XUPacketBuffer buffer) {
			buffer.writeDouble(x);
			buffer.writeDouble(y);
			buffer.writeDouble(z);
		}

		public void readFromPacket(XUPacketBuffer buffer) {
			x = buffer.readDouble();
			y = buffer.readDouble();
			z = buffer.readDouble();
		}
	}

	public static abstract class NBTObject<V, V_NBT extends NBTBase> implements INBTSerializable<V_NBT> {
		public V value;

		public NBTObject(V value) {
			this.value = value;
		}

		@Override
		public V_NBT serializeNBT() {
			return serialize(value);
		}

		protected abstract V_NBT serialize(V value);

		@Override
		public void deserializeNBT(V_NBT nbt) {
			value = deserialize(nbt);
		}

		protected abstract V deserialize(V_NBT nbt);
	}

	public static class NBTMutableBlockPos extends BlockPos.MutableBlockPos implements INBTSerializable<NBTTagLong> {

		@Override
		public NBTTagLong serializeNBT() {
			return new NBTTagLong(toLong());
		}

		@Override
		public void deserializeNBT(NBTTagLong nbt) {
			setPos(BlockPos.fromLong(nbt.getLong()));
		}
	}

	public static class NBTCollection<E, C extends Collection<E>, T extends NBTBase> implements INBTSerializable<NBTTagList> {
		public final C collection;
		public final Function<E, T> serializer;
		public final Function<T, E> deserializer;

		public NBTCollection(C collection, Function<E, T> serializer, Function<T, E> deserializer) {
			this.collection = collection;
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		@Override
		public NBTTagList serializeNBT() {
			NBTTagList list = new NBTTagList();
			for (E e : collection) {
				list.appendTag(serializer.apply(e));
			}
			return list;
		}

		@Override
		public void deserializeNBT(NBTTagList nbt) {
			collection.clear();
			for (int i = 0; i < nbt.tagCount(); i++) {
				T base = (T) nbt.get(i);
				E e = deserializer.apply(base);
				collection.add(e);
			}
		}
	}

	public static class NBTCollectionSerializable<E extends INBTSerializable<? extends NBTBase>, C extends Collection<E>> implements INBTSerializable<NBTTagList> {
		public final C collection;
		private final Supplier<E> blankVersionSupplier;

		public NBTCollectionSerializable(C collection, Supplier<E> blankVersionSupplier) {
			this.collection = collection;
			this.blankVersionSupplier = blankVersionSupplier;
		}

		@Override
		public NBTTagList serializeNBT() {
			NBTTagList list = new NBTTagList();
			for (E e : collection) {
				NBTBase t = e.serializeNBT();
				list.appendTag(t);
			}
			return list;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void deserializeNBT(NBTTagList nbt) {
			collection.clear();
			int n = nbt.tagCount();
			for (int i = 0; i < n; i++) {
				E e = blankVersionSupplier.get();
				NBTBase base = nbt.get(i);
				((INBTSerializable) e).deserializeNBT(base);
				collection.add(e);
			}
		}
	}

	public static final class NBTBlockState implements INBTSerializable<NBTTagCompound> {
		@Nullable
		public IBlockState value;

		@Nullable
		public static IBlockState getBlockState(ResourceLocation location, int meta) {
			Block block;
			if (Block.REGISTRY.getKeys().contains(location)) {
				block = Block.REGISTRY.getObject(location);
			} else {
				location = new ResourceLocation(
						location.getResourceDomain().toLowerCase(Locale.ENGLISH),
						location.getResourcePath().toLowerCase(Locale.ENGLISH));
				if (Block.REGISTRY.getKeys().contains(location)) {
					block = Block.REGISTRY.getObject(location);
				} else {
					return null;
				}
			}

			return block.getStateFromMeta(meta);
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			if (value != null) {
				ResourceLocation nameForObject = Block.REGISTRY.getNameForObject(value.getBlock());
				nbt.setString("block", nameForObject.toString());
				nbt.setInteger("meta", value.getBlock().getMetaFromState(value));
			}

			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			if (!nbt.hasKey("block", Constants.NBT.TAG_STRING)) {
				value = null;
			} else {
				Block block;
				ResourceLocation location = new ResourceLocation(nbt.getString("block"));
				int meta = nbt.getInteger("meta");

				value = getBlockState(location, meta);
			}
		}
	}

	public static final class NBTChunkPos implements INBTSerializable<NBTTagLong> {
		public int x, z;

		public NBTChunkPos() {

		}

		public NBTChunkPos(int x, int z) {
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			NBTChunkPos that = (NBTChunkPos) o;

			return x == that.x && z == that.z;

		}

		@Override
		public int hashCode() {
			int result = x;
			result = 31 * result + z;
			return result;
		}

		@Override
		public NBTTagLong serializeNBT() {
			long c = ((long) x << 32) | (z & 0xFFFFFFFFL);

			return new NBTTagLong(c);
		}

		@Override
		public void deserializeNBT(NBTTagLong nbt) {
			long c = nbt.getLong();
			x = (int) (c >> 32);
			z = (int) c;
		}
	}

	public static class NBTEnumIntMap<T extends Enum<T>> implements INBTSerializable<NBTTagList> {
		public final TObjectIntHashMap<T> map = new TObjectIntHashMap<>(10, 0.5F, 0);
		private final Class<T> clazz;

		public NBTEnumIntMap(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public NBTTagList serializeNBT() {
			NBTTagList list = new NBTTagList();
			map.forEachEntry((a, b) -> {
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("value", a.name());
				compound.setInteger("amount", b);
				list.appendTag(compound);
				return true;
			});
			return list;
		}

		@Override
		public void deserializeNBT(NBTTagList list) {
			map.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				T value = Enum.valueOf(clazz, compound.getString("value"));
				int amount = compound.getInteger("amount");
				map.put(value, amount);
			}
		}
	}

	public static class NBTUUID implements INBTSerializable<NBTTagString> {
		public UUID value = null;

		@Override
		public NBTTagString serializeNBT() {

			return new NBTTagString(value.toString());
		}

		@Override
		public void deserializeNBT(NBTTagString nbt) {
			value = UUID.fromString(nbt.getString());
		}
	}
}
