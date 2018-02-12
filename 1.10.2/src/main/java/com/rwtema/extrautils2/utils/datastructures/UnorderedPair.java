package com.rwtema.extrautils2.utils.datastructures;

import java.util.Objects;

public abstract class UnorderedPair<T> {

	public static <E> UnorderedPair<E> of(E a, E b) {
		if (Objects.equals(a, b)) return new Unary<>(a);
		return new Diff<>(a, b);
	}

	public abstract T getA();

	public abstract T getB();

	private static class Unary<T> extends UnorderedPair<T> {
		final T key;

		private Unary(T key) {
			this.key = key;
		}

		@Override
		public T getA() {
			return key;
		}

		@Override
		public T getB() {
			return key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Unary)) return false;

			Unary<?> unary = (Unary<?>) o;

			return key != null ? key.equals(unary.key) : unary.key == null;
		}

		@Override
		public int hashCode() {
			return key != null ? key.hashCode() : 0;
		}
	}

	private static class Diff<T> extends UnorderedPair<T> {
		final T a;
		final T b;

		protected Diff(T a, T b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Diff)) return false;

			Diff<?> that = (Diff<?>) o;

			return Objects.equals(a, that.a) && Objects.equals(this.b, that.b) || Objects.equals(a, that.b) && Objects.equals(this.b, that.a);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(a) + Objects.hashCode(b);
		}

		@Override
		public T getA() {
			return a;
		}

		@Override
		public T getB() {
			return b;
		}
	}
}
