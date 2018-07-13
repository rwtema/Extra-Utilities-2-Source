package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Locale;

public abstract class ItemEntry<T extends Item> extends Entry<T> implements IItemStackMaker {
	public static HashMap<ResourceLocation, Item> itemMap = new HashMap<>();

	public ItemEntry(String name) {
		super(name);
	}

	@Override
	public void preInitRegister() {
		if ("item.null".equals(value.getUnlocalizedName()))
			value.setUnlocalizedName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH));
		if (value instanceof XUItem) {
			((XUItem) value).entry = this;
		} else {
			if (value instanceof IXUItem) {
				XUItem.items.add((IXUItem) value);
			}
		}
		value.setRegistryName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH));
		CompatHelper112.register(value);
		itemMap.put(value.getRegistryName(), value);
	}

	@Override
	public ItemStack newStack(int amount, int meta) {
		if (value == null) return StackHelper.empty();
		return new ItemStack(value, amount, meta);
	}

	public ItemStack newStackLowestDamage() {
		return newStack(1, value.getMaxDamage());
	}

	public ItemStack newWildcardStack() {
		return newStack(1, OreDictionary.WILDCARD_VALUE);
	}
}
