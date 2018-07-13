package com.rwtema.extrautils2.backend;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRegisterItemColors {


	@SideOnly(Side.CLIENT)
	default void addItemColors(ItemColors itemColors, BlockColors blockColors) {

	}
}
