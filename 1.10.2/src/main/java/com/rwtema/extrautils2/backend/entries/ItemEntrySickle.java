package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.items.ItemSickle;

import java.util.Locale;

public class ItemEntrySickle extends ItemEntry<ItemSickle> {
	public static final Object[] MATERIALS = new Object[]{
			"plankWood",
			"cobblestone",
			"ingotIron",
			"ingotGold",
			"gemDiamond"
	};
	private final int type;

	public ItemEntrySickle(int type) {
		super(ItemSickle.TYPE_NAMES[type]);
		this.type = type;
	}

	@Override
	public ItemSickle initValue() {
		return new ItemSickle(type);
	}

	@Override
	public void addRecipes() {
		addShaped(ItemSickle.TYPE_NAMES[type], newStack(1), " GG", "  G", "SGG", 'G', MATERIALS[type], 'S', "stickWood");
	}
}
