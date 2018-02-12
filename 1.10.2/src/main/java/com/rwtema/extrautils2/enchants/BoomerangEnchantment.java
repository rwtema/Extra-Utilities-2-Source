package com.rwtema.extrautils2.enchants;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class BoomerangEnchantment extends XUEnchantment {
	public static EnumEnchantmentType BOOMERANG_TYPE = getOrRegisterEnchantmentType("BOOMERANG");

	public BoomerangEnchantment(String name, int maxLevel, Rarity rarityIn) {
		super(name, maxLevel, rarityIn, BOOMERANG_TYPE, EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND);
		register();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return StackHelper.isNonNull(stack) && stack.getItem() == XU2Entries.boomerang.value;
	}

	@Override
	public boolean isAllowedOnBooks() {
		return false;
	}
}
