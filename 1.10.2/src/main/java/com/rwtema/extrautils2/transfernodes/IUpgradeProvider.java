package com.rwtema.extrautils2.transfernodes;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IUpgradeProvider {
	@Nullable
	Upgrade getUpgrade(ItemStack stack);
}
