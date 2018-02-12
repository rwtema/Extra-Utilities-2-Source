package com.rwtema.extrautils2.utils.datastructures;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public interface NullableKey<T> {
	NullableKey<?> NULL = new NullableKey<Object>() {
		@Override
		public Object get() {
			return null;
		}

		@Override
		public boolean isNull() {
			return true;
		}
	};

	static <T extends NullableKey<T>> NullableKey<T> getKey(T t) {
		if (t == null) return (NullableKey<T>) NULL;
		return t;
	}

	@Nullable
	default T get() {
		return (T) this;
	}

	default boolean isNull() {
		return false;
	}
}
