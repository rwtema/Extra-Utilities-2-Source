package com.rwtema.extrautils2.utils;

import java.util.function.Predicate;

public class Inversion<T> implements Predicate<T> {
	private final Predicate<T> predicate;

	public Inversion(Predicate<T> predicate) {
		this.predicate = predicate;
	}

	public static <T> Predicate<T> of(Predicate<T> predicate) {
		if (predicate instanceof Inversion) {
			return predicate.negate();
		}

		return new Inversion<T>(predicate);
	}

	@Override
	public boolean test(T t) {
		return !predicate.test(t);
	}

	@Override
	public Predicate<T> negate() {
		return predicate;
	}
}
