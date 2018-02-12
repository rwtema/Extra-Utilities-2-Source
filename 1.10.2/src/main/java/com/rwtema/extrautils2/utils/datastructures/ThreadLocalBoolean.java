package com.rwtema.extrautils2.utils.datastructures;

public class ThreadLocalBoolean extends ThreadLocal<Boolean> {
	public final Boolean _default;

	public ThreadLocalBoolean(boolean aDefault) {
		_default = aDefault;
	}

	@Override
	protected Boolean initialValue() {
		return _default;
	}
}
