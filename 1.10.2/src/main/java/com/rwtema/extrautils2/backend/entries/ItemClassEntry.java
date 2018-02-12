package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.item.Item;

public class ItemClassEntry<T extends Item> extends ItemEntry<T> {
	Class<T> clazz;


	public ItemClassEntry(Class<T> clazz) {
		this(StringHelper.erasePrefix(clazz.getSimpleName(), "Item"), clazz);
	}

	public ItemClassEntry(String item, Class<T> clazz) {
		super(item);
		this.clazz = clazz;
	}

	@Override
	public T initValue() {
		try {
			return clazz.newInstance();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			throw new RuntimeException("Could not init " + clazz, throwable);
		}
	}
}
