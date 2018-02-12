package com.rwtema.extrautils2.utils.datastructures;

import java.util.concurrent.atomic.AtomicInteger;

public class UnKey {
	static final AtomicInteger hashcode = new AtomicInteger();

	@Override
	public boolean equals(Object obj) {
		return false;
	}

	@Override
	public int hashCode() {
		return hashcode.getAndIncrement();
	}
}
