package com.rwtema.extrautils2.compatibility;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public abstract class TESRCompat<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
	@Override
	public void func_180535_a(T p_180535_1_, double p_180535_2_, double p_180535_4_, double p_180535_6_, float p_180535_8_, int p_180535_9_) {
		render(p_180535_1_, p_180535_2_, p_180535_4_, p_180535_6_, p_180535_8_, p_180535_9_, 1);
	}

	public abstract void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha);


}
