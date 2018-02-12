package com.rwtema.extrautils2.utils.datastructures;

public class ThreadLocalInteger extends ThreadLocal<Integer> {
	final Integer _default;

	public ThreadLocalInteger(int _default) {
		this._default = _default;
	}

	@Override
	protected Integer initialValue() {
		return _default;
	}
}
