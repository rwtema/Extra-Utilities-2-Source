package com.rwtema.extrautils2.power.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IPlayerPowerCreator {
	PlayerPower createPower(EntityPlayer player, ItemStack params);

	default boolean shouldOverride(PlayerPower playerPower, EntityPlayer player, ItemStack stack, boolean isSelected){
		return false;
	}
}
