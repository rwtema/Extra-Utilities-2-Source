package com.rwtema.extrautils2.backend;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ClientFunction<R, T> {
	@SideOnly(Side.CLIENT)
	public abstract R apply(T t);
}
