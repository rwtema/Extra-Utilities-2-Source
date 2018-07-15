package com.rwtema.extrautils2;

import com.rwtema.extrautils2.backend.entries.ItemClassEntry;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.blocks.LuxColors;
import com.rwtema.extrautils2.blocks.TreeIronWoods;
import com.rwtema.extrautils2.blocks.XUTree;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.items.ItemLuxSaber;
import net.minecraft.item.ItemStack;

import java.util.Locale;


@SuppressWarnings("unused")
public class XU2Entries112 {
	public static final ItemClassEntry<ItemLuxSaber> itemLuxSaber = new ItemClassEntry<ItemLuxSaber>(ItemLuxSaber.class) {
		@Override
		public void addRecipes() {

			ItemStack crystal = XU2Entries.sunCrystal.newStack(1, 0);
			for (int i = 0; i < LuxColors.values().length; i++) {
				LuxColors luxColors = LuxColors.values()[i];
				String glass = "blockGlass" + luxColors.name().substring(0, 1).toUpperCase(Locale.ENGLISH)
						+ luxColors.name().substring(1).toLowerCase(Locale.ENGLISH);
				addShaped("lux_saber_" + luxColors.name().toLowerCase(Locale.ENGLISH),
						newStack(1, i),
						"igi", "ici", "ibi",
						'i', ItemIngredients.Type.EVIL_INFUSED_INGOT,
						'g', glass,
						'c', crystal,
						'b', XU2Entries.redstoneCrystal);
			}
		}
	};

	public static final XUTree tree = new TreeIronWoods();

	public static void init() {

	}
}
