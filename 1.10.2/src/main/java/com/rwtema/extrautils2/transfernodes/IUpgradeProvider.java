package com.rwtema.extrautils2.transfernodes;

import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;

public interface IUpgradeProvider {
	@Nullable
	Upgrade getUpgrade(ItemStack stack);
}
