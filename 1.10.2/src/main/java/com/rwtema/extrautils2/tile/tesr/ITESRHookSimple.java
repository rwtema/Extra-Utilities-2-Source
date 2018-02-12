package com.rwtema.extrautils2.tile.tesr;

import com.rwtema.extrautils2.tile.XUTile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITESRHookSimple<T extends XUTile> {
	@SideOnly(Side.CLIENT)
	default boolean isGlobalRenderer() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	void renderTileEntityAt(T tile, double x, double y, double z, float partialTicks, int destroyStage);
}
