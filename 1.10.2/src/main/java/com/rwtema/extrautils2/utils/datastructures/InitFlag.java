package com.rwtema.extrautils2.utils.datastructures;

import java.util.concurrent.atomic.AtomicBoolean;

public class InitFlag {
	private AtomicBoolean flag = new AtomicBoolean();

	public boolean init() {
		AtomicBoolean flag = this.flag;
		if (flag == null || flag.get() || !flag.compareAndSet(false, true)) return true;
		this.flag = null;
		return true;
	}
}
