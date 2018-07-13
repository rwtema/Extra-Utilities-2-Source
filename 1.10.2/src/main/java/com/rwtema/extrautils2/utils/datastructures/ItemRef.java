package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.items.itemmatching.IMatcher;
import com.rwtema.extrautils2.items.itemmatching.IMatcherMaker;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.NBTCopyHelper;
import com.rwtema.extrautils2.utils.helpers.NullHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public abstract class ItemRef implements IMatcher, IItemStackMaker, IMatcherMaker {
	public static final ItemRef NULL = new NullRef();

	private static final LoadingCache<Item, ItemRef> loadingCache = CacheBuilder.newBuilder().maximumSize(200).build(
			CacheLoader.from(t -> {
				if (t == null) return NULL;
				return new Simple(t);
			}));

	private ItemRef() {

	}

	public static ItemRef read(XUPacketBuffer buffer) {
		byte b = buffer.readByte();
		switch (b) {
			default:
			case 0:
				return NULL;
			case 1:
				return Simple.readFromPacket(buffer);
			case 2:
				return Meta.readFromPacket(buffer);
			case 3:
				return SimpleNBT.readFromPacket(buffer);
			case 4:
				return MetaNBT.readFromPacket(buffer);
		}
	}

	public static ItemRef wrapCrafting(ItemStack stack) {
		if (StackHelper.isNull(stack)) return NULL;
		Item item = getItem(stack);
		if (item == null) return NULL;
		if ((item.getHasSubtypes()) && stack.getMetadata() != OreDictionary.WILDCARD_VALUE) {
			return new Meta(item, stack.getItemDamage());
		}
		return createSimpleItem(item);
	}

	public static ItemRef wrapNoNBT(ItemStack stack) {
		if (StackHelper.isNull(stack)) return NULL;
		Item item = getItem(stack);
		if (item == null) return NULL;

		if (item.getHasSubtypes() || item.isDamageable()) {
			return new Meta(item, stack.getItemDamage());
		} else {
			return createSimpleItem(item);
		}
	}

	@Nullable
	public static Item getItem(ItemStack stack) {
		return NullHelper.nullable(stack.getItem());
	}

	public static ItemRef wrap(ItemStack stack) {
		if (StackHelper.isNull(stack)) return NULL;
		Item item = getItem(stack);
		if (item == null) return NULL;

		if (item.getHasSubtypes() || item.isDamageable()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
				return new Meta(item, stack.getItemDamage());
			else
				return new MetaNBT(item, stack.getItemDamage(), nbt);
		} else {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
				return createSimpleItem(item);
			else
				return new SimpleNBT(item, nbt);
		}
	}

	@Nonnull
	private static ItemRef createSimpleItem(@Nonnull Item item) {
		return loadingCache.getUnchecked(item);
	}

	public static ItemRef wrap(Item item) {
		return createSimpleItem(item);
	}

	public static ItemRef wrap(Block block) {
		Item item = Item.getItemFromBlock(block);
		if (item == StackHelper.nullItem()) return NULL;
		return createSimpleItem(item);
	}

	@Override
	public ItemStack newStack() {
		return createItemStack(1);
	}

	public ItemRef toCraftingVersion() {
		return this;
	}

	public ItemRef toNoMetaVersion() {
		return this;
	}

	@Override
	public boolean matchesItemStack(@Nullable ItemStack input) {
		return StackHelper.isNonNull(input) && equalsItemStack(input);
	}

	public ItemStack createItemStack(int amount) {
		ItemStack itemStack = new ItemStack(getItem(), amount, getMeta());
		itemStack.setTagCompound(getTagCompound());
		return itemStack;
	}

	public abstract Item getItem();

	public abstract int getMeta();

	public abstract NBTTagCompound getTagCompound();

	public abstract int getTagHash();

	public void write(XUPacketBuffer buffer) {
		writeToPacket(buffer);
	}

	protected abstract void writeToPacket(XUPacketBuffer buffer);

	public abstract boolean equalsItemStack(ItemStack stack);

	public abstract boolean hasMeta();

	public String getDisplayName() {
		ItemStack itemStack = createItemStack(1);
		return itemStack.getDisplayName();
	}

	public int getMaxStackSize() {
		return createItemStack(1).getMaxStackSize();
	}

	@Nonnull
	public Collection<ItemStack> getSubItems() {
		return ImmutableList.of(createItemStack(1));
	}

	@Override
	public ItemStack getMainStack() {
		return newStack();
	}

	private static final class Simple extends ItemRef {
		@Nonnull
		private final Item item;

		public Simple(@Nonnull Item item) {
			this.item = item;
		}

		public static ItemRef readFromPacket(XUPacketBuffer buffer) {
			Item item = NullHelper.nullable(Item.getItemById(buffer.readUnsignedShort()));
			if (item == null) return NULL;
			return createSimpleItem(item);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null) return false;

			if (Simple.class != o.getClass()) return false;

			Simple simple = (Simple) o;

			return item == simple.item;

		}

		@Override
		public int hashCode() {
			return System.identityHashCode(item);
		}

		@Nonnull
		@Override
		public Item getItem() {
			return item;
		}

		@Override
		public int getMeta() {
			return 0;
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
			buffer.writeByte(1);
			buffer.writeShort(Item.getIdFromItem(item));
		}

		@Override
		public boolean equalsItemStack(ItemStack stack) {
			return StackHelper.isNonNull(stack) && item == getItem(stack) && !stack.hasTagCompound();
		}

		@Override
		public boolean hasMeta() {
			return false;
		}

		@Nonnull
		@Override
		public Collection<ItemStack> getSubItems() {
			return ExtraUtils2.proxy.getSubItems(item);
		}
	}

	private static final class Meta extends ItemRef {
		@Nonnull
		private final Item item;
		private final int meta;

		public Meta(@Nonnull Item item, int meta) {
			this.item = item;
			this.meta = meta;
		}

		public static ItemRef readFromPacket(XUPacketBuffer buffer) {
			Item item = NullHelper.nullable(Item.getItemById(buffer.readShort()));
			int damage = buffer.readShort();
			if (item == null) return NULL;
			return new Meta(item, damage);
		}

		@Override
		public ItemRef toCraftingVersion() {
			if (!item.getHasSubtypes() || meta == OreDictionary.WILDCARD_VALUE ) return createSimpleItem(item);
			return this;
		}

		@Override
		public ItemRef toNoMetaVersion() {
			return createSimpleItem(item);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null) return false;

			if (Meta.class != o.getClass()) return false;

			Meta meta = (Meta) o;

			return this.meta == meta.meta && item == meta.item;

		}

		@Override
		public int hashCode() {
			int result = System.identityHashCode(item);
			result = 31 * result + meta;
			return result;
		}

		@Override
		public boolean equalsItemStack(ItemStack stack) {
			return StackHelper.isNonNull(stack) && item == getItem(stack) && meta == stack.getItemDamage() && !stack.hasTagCompound();
		}

		@Override
		public boolean hasMeta() {
			return true;
		}

		@Nonnull
		@Override
		public Item getItem() {
			return item;
		}

		@Override
		public int getMeta() {
			return meta;
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
			buffer.writeByte(2);
			buffer.writeShort(Item.getIdFromItem(item));
			buffer.writeShort(meta);
		}
	}

	private static class SimpleNBT extends ItemRef {
		@Nonnull
		protected final Item item;
		@Nonnull
		protected final NBTTagCompound tag;
		protected final int tagHash;
		byte[] packetBytes = null;

		public SimpleNBT(@Nonnull Item item, @Nonnull NBTTagCompound tag) {
			this.item = item;
			NBTCopyHelper.ResultNBT resultNBT = NBTCopyHelper.copyAndHashNBT(tag);
			this.tag = resultNBT.copy;
			this.tagHash = resultNBT.hash;
		}

		public SimpleNBT(@Nonnull Item item, @Nonnull NBTTagCompound tag, int tagHash) {
			this.item = item;
			this.tag = tag;
			this.tagHash = tagHash;
		}

		public static ItemRef readFromPacket(XUPacketBuffer buffer) {
			Item item = NullHelper.nullable(Item.getItemById(buffer.readUnsignedShort()));
			int tagHash = buffer.readInt();
			NBTTagCompound nbt = buffer.readNBT();
			if (item == null) return NULL;
			if(nbt == null) return ItemRef.wrap(item);
			return new SimpleNBT(item, nbt, tagHash);
		}

		@Override
		public ItemRef toCraftingVersion() {
			return createSimpleItem(item);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null) return false;

			if (SimpleNBT.class != o.getClass()) return false;

			SimpleNBT simpleNbt = (SimpleNBT) o;

			if (item == simpleNbt.item && tagHash == simpleNbt.tagHash && tagEquals(simpleNbt.tag)) {
				if (packetBytes == null) {
					packetBytes = simpleNbt.packetBytes;
				} else if (simpleNbt.packetBytes == null) {
					simpleNbt.packetBytes = packetBytes;
				}
				return true;
			}
			return false;
		}

		public boolean tagEquals(NBTTagCompound otherTag) {
			return this.tag == otherTag || NBTCopyHelper.equalNBT(this.tag, otherTag);
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(item) * 31 + tagHash;
		}

		@Override
		public boolean equalsItemStack(ItemStack stack) {
			return StackHelper.isNonNull(stack) && item == getItem(stack) && stack.hasTagCompound() && tagEquals(stack.getTagCompound());
		}

		@Override
		public boolean hasMeta() {
			return false;
		}

		@Nonnull
		@Override
		public Item getItem() {
			return item;
		}

		@Override
		public int getMeta() {
			return 0;
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
		protected void writeToPacket(XUPacketBuffer buffer) {
			buffer.writeByte(3);
			buffer.writeShort(Item.getIdFromItem(item));
			writeNBT(buffer);
		}

		protected void writeNBT(XUPacketBuffer buffer) {
			buffer.writeInt(tagHash);

			if (packetBytes == null) {

				if (item.getShareTag() || item.isDamageable()) {
					ItemStack stack = new ItemStack(item);
					stack.setTagCompound(tag);
					NBTTagCompound shareTag = NullHelper.nullable(item.getNBTShareTag(stack));
					if (shareTag == null)
						packetBytes = new byte[0];
					else {
						try {
							packetBytes = XUPacketBuffer.compress(shareTag);
						} catch (IOException e) {
							throw Throwables.propagate(e);
						}
					}
				} else {
					packetBytes = new byte[0];
				}

				if (packetBytes == null) {
					buffer.writeShort(0);
					return;
				}
			}

			buffer.writeShort(packetBytes.length);
			buffer.writeBytes(packetBytes);
		}
	}


	private static final class MetaNBT extends SimpleNBT {
		private final int meta;

		public MetaNBT(@Nonnull Item item, int meta, @Nonnull NBTTagCompound tag) {
			super(item, tag);
			this.meta = meta;
		}

		public MetaNBT(@Nonnull Item item, int meta, @Nonnull NBTTagCompound tag, int tagHash) {
			super(item, tag, tagHash);
			this.meta = meta;
		}

		public static ItemRef readFromPacket(XUPacketBuffer buffer) {
			Item item = NullHelper.nullable(Item.getItemById(buffer.readUnsignedShort()));
			int meta = buffer.readShort();
			int tagHash = buffer.readInt();
			NBTTagCompound nbt = buffer.readNBT();
			if (item == null) return NULL;
			if(nbt == null) return new Meta(item, meta);
			return new MetaNBT(item, meta, nbt, tagHash);
		}

		@Override
		public ItemRef toNoMetaVersion() {
			return createSimpleItem(item);
		}

		@Override
		public ItemRef toCraftingVersion() {
			if (!item.getHasSubtypes()) return createSimpleItem(item);
			return new ItemRef.Meta(item, meta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null) return false;

			if (MetaNBT.class != o.getClass()) return false;

			MetaNBT metaNBT = (MetaNBT) o;

			return meta == metaNBT.meta && tagHash == metaNBT.tagHash && item == metaNBT.item && tagEquals(metaNBT.tag);
		}

		@Override
		public int getMeta() {
			return meta;
		}

		@Override
		public int hashCode() {
			int result = System.identityHashCode(item) + 1;
			result = 31 * result + tagHash;
			result = 31 * result + meta;
			return result;
		}

		@Override
		public boolean equalsItemStack(ItemStack stack) {
			return StackHelper.isNonNull(stack) && item == getItem(stack) && meta == stack.getItemDamage() && stack.hasTagCompound() && tagEquals(stack.getTagCompound());
		}

		@Override
		protected void writeToPacket(XUPacketBuffer buffer) {
			buffer.writeByte(4);
			buffer.writeShort(Item.getIdFromItem(item));
			buffer.writeShort(meta);
			writeNBT(buffer);
		}
	}

	private static class NullRef extends ItemRef {
		@Override
		public boolean matchesItemStack(@Nullable ItemStack input) {
			return StackHelper.isNull(input) || super.matchesItemStack(input);
		}

		@Override
		public int getMaxStackSize() {
			return 0;
		}

		@Nonnull
		@Override
		public Collection<ItemStack> getSubItems() {
			return Collections.emptyList();
		}

		@Override
		public ItemStack createItemStack(int amount) {
			return StackHelper.empty();
		}

		@Override
		public String getDisplayName() {
			return "[Null]";
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public Item getItem() {
			return null;
		}

		@Override
		public int getMeta() {
			return 0;
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
		public boolean equalsItemStack(ItemStack stack) {
			return StackHelper.isNull(stack) || getItem(stack) == null;
		}

		@Override
		public boolean hasMeta() {
			return false;
		}
	}
}
