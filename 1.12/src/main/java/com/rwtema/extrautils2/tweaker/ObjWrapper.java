package com.rwtema.extrautils2.tweaker;

import java.util.Objects;

public class ObjWrapper<T> {
	public final T object;

	public ObjWrapper(T object) {
		this.object = object;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ObjWrapper<?> that = (ObjWrapper<?>) o;
		return Objects.equals(object, that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object);
	}

	public T getInternal() {
		return object;
	}
}
