package com.rwtema.extrautils2.gui.backend;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IWidgetCustomJEIIngredient {
	@SideOnly(Side.CLIENT)
	Object getJEIIngredient();
}
