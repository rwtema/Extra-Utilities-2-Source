package com.rwtema.extrautils2.tile.tesr;

import com.rwtema.extrautils2.compatibility.TESRCompat;
import com.rwtema.extrautils2.tile.XUTile;

public class XUTESRHookSimple<T extends XUTile & ITESRHookSimple<T>> extends TESRCompat<T> {

	@Override
	public boolean isGlobalRenderer(T te) {
		return te.isGlobalRenderer();
	}

	@Override
	public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		te.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
	}
}
