package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.potion.PotionsHelper;
import com.rwtema.extrautils2.potion.XUPotion;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

public abstract class PotionEntry<T extends XUPotion> extends Entry<T> {
	final String tooltip;

	public PotionEntry(String name, String tooltip) {
		super(name);
		this.tooltip = tooltip;
	}

	@Override
	public void postInit() {
		PotionsHelper.registerPotion(value, tooltip);
		registerTypesAndRecipes();
	}

	public abstract void registerTypesAndRecipes();

	@Override
	public ItemStack newStack(int amount, int meta) {
		for (PotionType potionType : PotionType.REGISTRY) {
			List<PotionEffect> effects = potionType.getEffects();
			if (effects.size() == 1) {
				if (effects.get(0).getPotion() == value) {
					return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potionType);
				}
			}
		}

		throw new IllegalStateException();
	}
}
