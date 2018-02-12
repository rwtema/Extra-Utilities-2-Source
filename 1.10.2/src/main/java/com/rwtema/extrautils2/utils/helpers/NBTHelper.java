package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class NBTHelper {
	public static NBTTagCompound getPersistentTag(EntityPlayer player) {
		return getOrInitTagCompound(player.getEntityData(), EntityPlayer.PERSISTED_NBT_TAG, null);
	}

	public static NBTTagCompound getOrCreatePersistentTag(EntityPlayer player) {
		return getOrInitTagCompound(player.getEntityData(), EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
	}

	public static NBTTagCompound getOrInitTagCompound(NBTTagCompound parent, String key) {
		return getOrInitTagCompound(parent, key, null);
	}

	public static NBTTagCompound getOrInitTagCompound(NBTTagCompound parent, String key, NBTTagCompound defaultTag) {
		if (parent.hasKey(key, Constants.NBT.TAG_COMPOUND))
			return parent.getCompoundTag(key);

		if (defaultTag == null)
			defaultTag = new NBTTagCompound();
		else
			defaultTag = defaultTag.copy();

		parent.setTag(key, defaultTag);
		return defaultTag;
	}


	public static NBTTagCompound proifleToNBT(GameProfile profile) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Name", profile.getName());
		UUID id = profile.getId();
		if (id != null) {
			tag.setLong("UUIDL", id.getLeastSignificantBits());
			tag.setLong("UUIDU", id.getMostSignificantBits());
		}
		return tag;
	}

	public static GameProfile profileFromNBT(NBTTagCompound tag) {
		String name = tag.getString("Name");
		UUID uuid = null;
		if (tag.hasKey("UUIDL")) {
			uuid = new UUID(tag.getLong("UUIDU"), tag.getLong("UUIDL"));
		} else {
			if (StringUtils.isBlank(name)) return null;
		}
		return new GameProfile(uuid, name);

	}


	public static Iterable<NBTTagCompound> iterateNBTTagList(final NBTTagList list) {
		return new Iterable<NBTTagCompound>() {
			@Override
			public Iterator<NBTTagCompound> iterator() {
				return new Iterator<NBTTagCompound>() {
					private int i = 0;

					@Override
					public boolean hasNext() {
						return i < list.tagCount();
					}

					@Override
					public NBTTagCompound next() {
						return list.getCompoundTagAt(i++);
					}

					@Override
					public void remove() {
						list.removeTag(i--);
					}
				};
			}
		};
	}

	public static NBTTagCompound getOrInitTagCompound(ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();
		if (tags != null) return tags;
		tags = new NBTTagCompound();
		stack.setTagCompound(tags);
		return tags;
	}

	public static boolean hasPersistantNBT(Entity entity) {
		return entity.getEntityData().hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10);
	}

	public static NBTTagCompound getPersistantNBT(Entity entity) {
		NBTTagCompound t = entity.getEntityData();
		return getOrInitTagCompound(t, EntityPlayer.PERSISTED_NBT_TAG);
	}

	public static NBTTagCompound blockPosToNBT(BlockPos pos) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", pos.getX());
		nbt.setByte("y", (byte) pos.getY());
		nbt.setInteger("z", pos.getZ());
		return nbt;
	}

	public static BlockPos nbtToBlockPos(NBTTagCompound nbt) {
		int x = nbt.getInteger("x");
		int y = nbt.getByte("y") & 0xff;
		int z = nbt.getInteger("z");
		return new BlockPos(x, y, z);
	}

	public static <T extends NBTBase> void deserializeSubTag(INBTSerializable<T> serializable, NBTTagCompound parent, String key) {
		T tag = (T) parent.getTag(key);
		if (tag != null)
			serializable.deserializeNBT(tag);
	}


	public static List<String> getFluidStackRecipeEntry(NBTBase base) {
		switch (base.getId()) {
			case Constants.NBT.TAG_STRING:
				return ImmutableList.of(((NBTTagString) base).getString());
			case Constants.NBT.TAG_COMPOUND:
				return ImmutableList.of(FluidStack.loadFluidStackFromNBT((NBTTagCompound) base).getFluid().getName());
			case Constants.NBT.TAG_LIST:
				ArrayList<String> objects = new ArrayList<>();
				NBTTagList list = (NBTTagList) base;
				for (int i = 0; i < list.tagCount(); i++) {
					NBTBase nbtBase = list.get(i);
					objects.addAll(getFluidStackRecipeEntry(nbtBase));
				}
				return objects;
			default:
				throw new IllegalArgumentException("Unable to process fluidstack tag, " + base);
		}
	}

	public static List<ItemStack> getItemStackRecipeEntry(NBTBase base) {
		switch (base.getId()) {
			case Constants.NBT.TAG_STRING:
				return OreDictionary.getOres(((NBTTagString) base).getString());
			case Constants.NBT.TAG_COMPOUND:
				return ImmutableList.of(StackHelper.loadFromNBT((NBTTagCompound) base));
			case Constants.NBT.TAG_LIST:
				ArrayList<ItemStack> objects = new ArrayList<>();
				NBTTagList list = (NBTTagList) base;
				for (int i = 0; i < list.tagCount(); i++) {
					NBTBase nbtBase = list.get(i);
					objects.addAll(getItemStackRecipeEntry(nbtBase));
				}
				return objects;
			default:
				throw new IllegalArgumentException("Unable to process itemstack/ore tag, " + base);
		}
	}

	@Nullable
	public static String getStringOrNull(NBTTagCompound nbt, String key) {
		return nbt.hasKey("background_texture") ? nbt.getString(key) : null;
	}

	public static <T, N extends NBTBase> NBTTagList createList(Collection<T> list, Function<T, N> convertor) {
		NBTTagList nbtTagList = new NBTTagList();
		for (T t : list) {
			N tag = convertor.apply(t);
			nbtTagList.appendTag(tag);
		}
		return nbtTagList;
	}

	public static <T extends INBTSerializable<N>, N extends NBTBase> List<T> processList(NBTTagList list, Supplier<T> blankSupplier) {
		ArrayList<T> objects = Lists.newArrayListWithExpectedSize(list.tagCount());
		for (int i = 0; i < list.tagCount(); i++) {
			T newInstance = blankSupplier.get();
			newInstance.deserializeNBT((N) list.get(i));
			objects.add(newInstance);
		}
		return objects;
	}

	public static <T, N extends NBTBase> List<T> processList(NBTTagList list, Function<N, T> convertor) {
		ArrayList<T> objects = Lists.newArrayListWithExpectedSize(list.tagCount());
		for (int i = 0; i < list.tagCount(); i++) {
			objects.add(convertor.apply((N) list.get(i)));
		}
		return objects;
	}

	public static NBTTagList getTagListAnyType(NBTTagCompound tag, String key) {
		NBTBase base = NullHelper.nullable(tag.getTag(key));
		if (base == null || base.getId() != Constants.NBT.TAG_LIST) {
			return new NBTTagList();
		}
		return (NBTTagList) base;
	}


	@Nonnull
	public static NBTChainBuilder builder() {
		return new NBTChainBuilder(new NBTTagCompound());
	}

	public static class NBTChainBuilder {
		final NBTTagCompound tag;

		public NBTTagCompound build(){
			return tag;
		}


		public NBTChainBuilder() {
			this(new NBTTagCompound());
		}

		public NBTChainBuilder(NBTTagCompound tag) {
			this.tag = tag;
		}

		@Nonnull
		public NBTChainBuilder setTag(@Nonnull String key, @Nonnull NBTBase value) {
			tag.setTag(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setByte(@Nonnull String key, byte value) {
			tag.setByte(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setShort(@Nonnull String key, short value) {
			tag.setShort(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setInteger(@Nonnull String key, int value) {
			tag.setInteger(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setLong(@Nonnull String key, long value) {
			tag.setLong(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setUniqueId(String key, @Nonnull UUID value) {
			tag.setUniqueId(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setFloat(@Nonnull String key, float value) {
			tag.setFloat(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setDouble(@Nonnull String key, double value) {
			tag.setDouble(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setString(@Nonnull String key, @Nonnull String value) {
			tag.setString(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setByteArray(@Nonnull String key, @Nonnull byte[] value) {
			tag.setByteArray(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setIntArray(@Nonnull String key, @Nonnull int[] value) {
			tag.setIntArray(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setBoolean(@Nonnull String key, boolean value) {
			tag.setBoolean(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder merge(@Nonnull NBTTagCompound other) {
			tag.merge(other);
			return this;
		}
	}

}
