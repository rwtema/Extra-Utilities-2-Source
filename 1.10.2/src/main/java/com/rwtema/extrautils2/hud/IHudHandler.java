package com.rwtema.extrautils2.hud;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHudHandler {
	@SideOnly(Side.CLIENT)
	void render(GuiIngameForge hud, ScaledResolution resolution, float partialTicks);
}
