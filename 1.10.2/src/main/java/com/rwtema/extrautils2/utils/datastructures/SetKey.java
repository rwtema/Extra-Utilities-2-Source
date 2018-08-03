package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class SetKey<T> {
	final Set<T> base;

	public SetKey(Set<T> base) {
		this.base = ImmutableSet.copyOf(base);
	}

	@SafeVarargs
	public static <T> SetKey<T> of(T... elements) {
		return new SetKey<>(ImmutableSet.copyOf(elements));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SetKey)) return false;

		SetKey<?> setKey = (SetKey<?>) o;

		return base.equals(setKey.base);
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}
}
