package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

import java.util.Set;

public class NBTCopyHelper {

	public static ResultNBT copyAndHashNBT(NBTTagCompound tag) {
		int hash = Constants.NBT.TAG_COMPOUND;
		NBTTagCompound copy = new NBTTagCompound();
		for (String s : tag.getKeySet()) {
			Result result = copyAndHash(tag.getTag(s));
			hash += s.hashCode() ^ result.hash;
			copy.setTag(s, result.base);
		}

		return new ResultNBT(copy, hash);
	}

	public static Result copyAndHash(NBTBase base) {
		byte id = base.getId();
		switch (id) {
			case Constants.NBT.TAG_BYTE: {
				NBTTagByte cache = Cache.getCachedByte(((NBTTagByte) base).getByte());
				return new Result(cache, cache.hashCode());
			}

			case Constants.NBT.TAG_INT: {
				NBTTagInt cache = Cache.toCachedInt((NBTTagInt) base);
				return new Result(cache, cache.hashCode());
			}

			case Constants.NBT.TAG_COMPOUND: {
				int hash = Constants.NBT.TAG_COMPOUND;
				NBTTagCompound copy = new NBTTagCompound();
				NBTTagCompound tag = (NBTTagCompound) base;
				for (String s : tag.getKeySet()) {
					Result result = copyAndHash(tag.getTag(s));
					hash += s.hashCode() ^ result.hash;
					copy.setTag(s, result.base);
				}
				return new Result(copy, hash);
			}

			case Constants.NBT.TAG_LIST: {
				NBTTagList list = (NBTTagList) base;
				int tagType = list.getTagType();
				if (tagType == 0) {
					return new Result(list.copy(), list.hashCode());
				} else {
					NBTTagList copy = new NBTTagList();
					int hash = Constants.NBT.TAG_LIST;
					for (int i = 0; i < list.tagCount(); i++) {
						Result result = copyAndHash(list.get(i));
						hash = hash * 31 + result.hash;
						copy.appendTag(result.base);
					}
					return new Result(copy, hash);
				}
			}

			case Constants.NBT.TAG_INT_ARRAY:
			case Constants.NBT.TAG_BYTE_ARRAY:
				return new Result(base.copy(), base.hashCode());

			default:
				return new Result(base, base.hashCode());
		}
	}

	public static boolean equal(NBTBase a, NBTBase b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		byte id = a.getId();
		if (id != b.getId()) return false;
		if (id == Constants.NBT.TAG_COMPOUND)
			return equalNBT((NBTTagCompound) a, (NBTTagCompound) b);
		else if (id == Constants.NBT.TAG_LIST)
			return equalTagList((NBTTagList) a, (NBTTagList) b);
		else
			return a.equals(b);
	}


	public static boolean equalTagList(NBTTagList a, NBTTagList b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		int tagCount = a.tagCount();
		if (tagCount != b.tagCount()) return false;
		if (a.getTagType() != b.getTagType()) return false;
		for (int i = 0; i < tagCount; i++) {
			if (!equal(a.get(i), b.get(i))) return false;
		}
		return true;
	}

	public static boolean equalNBT(NBTTagCompound a, NBTTagCompound b) {
		if (a == b) return true;
		if (a == null || b == null) return false;

		Set<String> aKeys = a.getKeySet();
		Set<String> bKeys = b.getKeySet();
		if (aKeys.size() != bKeys.size()) return false;

		for (String aKey : aKeys) {
			if (!equal(a.getTag(aKey), b.getTag(aKey))) return false;
		}
		return true;
	}

	public static class ResultNBT {
		public final NBTTagCompound copy;
		public final int hash;

		public ResultNBT(NBTTagCompound copy, int hash) {
			this.copy = copy;
			this.hash = hash;
		}
	}

	public static class Result {
		public final NBTBase base;
		public final int hash;

		public Result(NBTBase base, int hash) {
			this.base = base;
			this.hash = hash;
		}
	}

	public static class Cache {
		public static final NBTTagByte FALSE;
		public static final NBTTagByte TRUE;
		private static final NumbersCache[] NUM_CACHE = new NumbersCache[256];

		static {
			for (int i = 0; i < 256; i++) {
				NUM_CACHE[i] = new NumbersCache(i - 128);
			}

			FALSE = getCachedByte((byte) 0);
			TRUE = getCachedByte((byte) 1);
		}

		public static NBTTagByte getCachedByte(byte b) {
			return NUM_CACHE[(int) b + 128].byteValue;
		}

		public static NBTTagInt toCachedInt(NBTTagInt original) {
			int i = original.getInt();
			return i >= -128 && i <= 127 ? NUM_CACHE[i + 128].intValue : original;
		}

		public static NBTTagInt getInt(int i) {
			return i >= -128 && i <= 127 ? NUM_CACHE[i + 128].intValue : new NBTTagInt(i);

		}

		private static class NumbersCache {
			final NBTTagByte byteValue;
			final NBTTagShort shortValue;
			final NBTTagInt intValue;
			final NBTTagLong longValue;

			private NumbersCache(int n) {
				Validate.isTrue(n >= -128 && n <= 127);
				byteValue = new NBTTagByte((byte) n);
				shortValue = new NBTTagShort((short) n);
				intValue = new NBTTagInt(n);
				longValue = new NBTTagLong(n);
			}
		}
	}
}
