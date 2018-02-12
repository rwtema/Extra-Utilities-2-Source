package com.rwtema.extrautils2.utils.datastructures;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

// 🚳 No Bicycles allowed 🚳
public class AltStackStorage {
	static final byte FLAG_HASMETA = 1;
	static final byte FLAG_HASNBT = 2;
	static final byte FLAG_HASFORGECAPS = 4;
	static final byte FLAG_COUNT_ONE = 8;
	static final byte FLAG_COUNT_SIXTYFOUR = 16;
	static final byte[] EMPTY_ARRAY = new byte[0];
	private static final int mineCraftPrefixLength = "minecraft:".length();

	@Nonnull
	public static byte[] storeData(@Nullable ItemStack stack) {
		if (StackHelper.isNull(stack)) return EMPTY_ARRAY;
		NBTTagCompound nbt = stack.writeToNBT(new NBTTagCompound());
		String id = nbt.getString("id");
		if ("minecraft:air".equals(id)) {
			return EMPTY_ARRAY;
		}
		int count = nbt.getByte("Count");
		if (count == 0) return EMPTY_ARRAY;
		short damage = nbt.getShort("Damage");

		boolean isVanilla = id.startsWith("minecraft:") && id.indexOf(':', mineCraftPrefixLength) < 0;

		NBTTagCompound tag = nbt.hasKey("tag") ? nbt.getCompoundTag("tag") : null;
		NBTTagCompound forgeCaps = nbt.hasKey("ForgeCaps") ? nbt.getCompoundTag("ForgeCaps") : null;

		byte type = 0;

		if (damage != 0) type |= FLAG_HASMETA;
		if (tag != null) type |= FLAG_HASNBT;
		if (forgeCaps != null) type |= FLAG_HASFORGECAPS;
		if (count == 1) type |= FLAG_COUNT_ONE;
		if (count == 64) type |= FLAG_COUNT_SIXTYFOUR;

		ByteBuf data = Unpooled.buffer();
		XUPacketBuffer buffer = new XUPacketBuffer(data);
		data.writeByte(type);
		if (isVanilla) {
			buffer.writeSmallString(id.substring(mineCraftPrefixLength));
		} else {
			buffer.writeSmallString(id);
		}
		if (count != 1 && count != 64) {
			buffer.writeByte(count);
		}

		if (damage != 0) {
			buffer.writeShort(damage);
		}

		if (tag != null) {
			buffer.writeNBT(tag);
		}

		if (forgeCaps != null) {
			buffer.writeNBT(forgeCaps);
		}

		int readableBytes = data.readableBytes();
		byte[] b = new byte[readableBytes];
		data.readBytes(b);
		return b;
	}

	@Nullable
	public static ItemStack loadData(@Nonnull byte[] bytes) {
		if (bytes.length == 0) return StackHelper.empty();

		ByteBuf data = Unpooled.wrappedBuffer(bytes);
		XUPacketBuffer buffer = new XUPacketBuffer(data);
		byte type = buffer.readByte();
		String id = buffer.readSmallString();
		if (id.indexOf(':') < 0) {
			id = "minecraft:" + id;
		}

		byte count;
		if ((type & FLAG_COUNT_ONE) != 0) {
			count = 1;
		} else if ((type & FLAG_COUNT_SIXTYFOUR) != 0) {
			count = 64;
		} else {
			count = buffer.readByte();
		}

		short damage;
		if ((type & FLAG_HASMETA) != 0) {
			damage = buffer.readShort();
		} else {
			damage = 0;
		}

		NBTTagCompound tags;
		if ((type & FLAG_HASNBT) != 0) {
			try {
				tags = buffer.readNBTChecked();
			} catch (IOException e) {
				return StackHelper.empty();
			}
		} else {
			tags = null;
		}

		NBTTagCompound forgeCaps;
		if ((type & FLAG_HASFORGECAPS) != 0) {
			try {
				forgeCaps = buffer.readNBTChecked();
			} catch (IOException e) {
				return StackHelper.empty();
			}
		} else {
			forgeCaps = null;
		}

		if (data.readableBytes() > 0) {
			return StackHelper.empty();
		}

		NBTTagCompound finalTag = new NBTTagCompound();
		finalTag.setString("id", id);
		finalTag.setByte("Count", count);
		finalTag.setShort("Damage", damage);
		if (tags != null) {
			finalTag.setTag("tag", tags);
		}
		if (forgeCaps != null) {
			finalTag.setTag("ForgeCaps", forgeCaps);
		}

		return StackHelper.loadFromNBT(finalTag);
	}

	public static void test() {
//		Random random = new Random();
//		ArrayList<ItemStack> itemStackArrayList = new ArrayList<ItemStack>();
//		itemStackArrayList.add(null);
//		itemStackArrayList.add(new ItemStack(Items.COAL, 54));
//		itemStackArrayList.add(new ItemStack(Items.COAL, 64));
//		for (Item item : Item.REGISTRY) {
//			ArrayList<ItemStack> list = new ArrayList<>();
//			item.getSubItems(item, item.getCreativeTab(), list);
//			itemStackArrayList.addAll(list);
//			itemStackArrayList.addAll(list.stream().map(t -> ItemHandlerHelper.copyStackWithSize(t, 1)).collect(Collectors.toList()));
//			itemStackArrayList.addAll(list.stream().map(t -> ItemHandlerHelper.copyStackWithSize(t, 64)).collect(Collectors.toList()));
//			itemStackArrayList.addAll(list.stream().map(t -> ItemHandlerHelper.copyStackWithSize(t, 1 + random.nextInt(87))).collect(Collectors.toList()));
//		}
//
//		for (ItemStack stack : itemStackArrayList) {
//			byte[] bytes = storeData(stack);
//
//			ItemStack newStack = loadData(bytes);
//
//			if (!ItemStack.areItemStacksEqual(newStack, stack)) {
//				LogHelper.info(newStack + " <> " + stack);
//				bytes = storeData(stack);
//				newStack = loadData(bytes);
//			}
//		}
//		LogHelper.info("Complete");

	}
}
