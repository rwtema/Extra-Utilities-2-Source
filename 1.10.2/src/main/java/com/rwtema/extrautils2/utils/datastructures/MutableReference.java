package com.rwtema.extrautils2.utils.datastructures;

import javax.annotation.Nullable;

public interface MutableReference<T> {
	@Nullable
	T get();

	void set(@Nullable T t);

	class Impl<T> implements MutableReference<T> {
		@Nullable
		private T t;

		public Impl(T initialValue) {
			this.set(initialValue);
		}

		@Nullable
		public T get() {
			return t;
		}

		public void set(@Nullable T t) {
			this.t = t;
		}
	}
}
