package com.rwtema.extrautils2.utils.datastructures;

import net.minecraft.item.Item;

import java.util.Comparator;

public abstract class ItemRefComparator implements Comparator<ItemRef> {
	public static final ItemRefComparator id = new ItemRefComparator() {
		@Override
		public int doCompare(ItemRef o1, ItemRef o2) {
			int i = compareInt(Item.getIdFromItem(o1.getItem()), Item.getIdFromItem(o2.getItem()));
			if (i != 0) return i;
			i = compareInt(o1.getMeta(), o2.getMeta());
			if (i != 0) return i;
			return compareInt(o1.getTagHash(), o2.getTagHash());
		}
	};

	public static final ItemRefComparator names = new ItemRefComparator() {
		@Override
		public int doCompare(ItemRef o1, ItemRef o2) {
			int i = o1.getDisplayName().compareTo(o2.getDisplayName());
			if (i != 0)
				return i;
			return id.doCompare(o1, o2);
		}
	};

	@Override
	public int compare(ItemRef o1, ItemRef o2) {
		if (o1 == o2) return 0;
		if (o1 == ItemRef.NULL) return 1;
		if (o2 == ItemRef.NULL) return -1;

		return doCompare(o1, o2);
	}

	protected abstract int doCompare(ItemRef o1, ItemRef o2);

	public int compareInt(int a, int b) {
		return (a < b) ? -1 : ((a == b) ? 0 : 1);
	}
}
