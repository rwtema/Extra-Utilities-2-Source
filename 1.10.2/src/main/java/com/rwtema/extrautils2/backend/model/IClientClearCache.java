package com.rwtema.extrautils2.backend.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IClientClearCache {

	@SideOnly(Side.CLIENT)
	void clientClear();
}
