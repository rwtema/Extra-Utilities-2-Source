package com.rwtema.extrautils2.enchants;

import com.google.common.collect.Sets;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;

public abstract class XUEnchantment extends Enchantment {
	public final String stripText;
	public final HashSet<Enchantment> blackList = new HashSet<>();
	private final int maxLevel;
	private int minEnchantLevel = 1;

	public XUEnchantment(String name, int maxLevel, Rarity rarityIn, EnumEnchantmentType typeIn, EntityEquipmentSlot... slots) {
		super(rarityIn, typeIn, slots);
		this.maxLevel = maxLevel;
		stripText = "xu." + Lang.stripText(name);
		Lang.translate("enchantment." + stripText, name);
		setName(stripText);
	}

	public static EnumEnchantmentType getOrRegisterEnchantmentType(String name) {
		for (EnumEnchantmentType type : EnumEnchantmentType.values()) {
			if (name.equalsIgnoreCase(type.name())) {
				return type;
			}
		}
		return CompatHelper.addEnchantmentType(name);
	}

	public static void makeMutuallyExclusive(XUEnchantment... enchantments) {
		HashSet<XUEnchantment> set = Sets.newHashSet(enchantments);
		for (XUEnchantment enchantment : enchantments) {
			enchantment.blackList.addAll(set);
		}
	}

	public static ItemStack addEnchantment(ItemStack itemStack, Enchantment enchantment, int level) {
		itemStack.addEnchantment(enchantment, level);
		return itemStack;
	}

	public void register() {
		setRegistryName(new ResourceLocation(ExtraUtils2.MODID, stripText));
		CompatHelper112.register(this);
	}

	public XUEnchantment setMinEnchantLevel(int minEnchantLevel) {
		this.minEnchantLevel = minEnchantLevel;
		return this;
	}

	@Override
	public abstract boolean canApplyAtEnchantingTable(ItemStack stack);

	@Override
	public int getMaxLevel() {
		return maxLevel;
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return minEnchantLevel + (enchantmentLevel * (30 - minEnchantLevel)) / maxLevel;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel + 1) + 1;
	}

	@Override
	public boolean canApplyTogether(Enchantment enchantment) {
		return this != enchantment && !blackList.contains(enchantment);
	}
}
