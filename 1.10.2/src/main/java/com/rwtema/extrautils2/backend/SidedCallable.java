package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.Callable;

public abstract class SidedCallable<V> implements Callable<V> {
	@Override
	public V call() throws Exception {
		return callUnchecked();
	}

	public V callUnchecked() {
		return ExtraUtils2.proxy.call(this);
	}

	@SideOnly(Side.CLIENT)
	public abstract V callClient();

	@SideOnly(Side.SERVER)
	public abstract V callServer();
}
