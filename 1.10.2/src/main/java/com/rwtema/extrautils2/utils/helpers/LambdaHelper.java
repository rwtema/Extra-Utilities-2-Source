package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LambdaHelper {
	public static <T> Consumer<T> ifelse(Predicate<T> predicate, Consumer<T> ifTrue, Consumer<T> ifFalse){
		return o -> (predicate.test(o) ? ifTrue : ifFalse).accept(o);
	}

	private static Predicate<?> ALWAYS_FALSE = new Predicate<Object>() {

		@Override
		public boolean test(Object o) {
			return false;
		}

		@Override
		public Predicate<Object> and(Predicate<? super Object> other) {
			return this;
		}

		@Override
		public Predicate<Object> negate() {
			return (Predicate<Object>) ALWAYS_TRUE;
		}

		@Override
		public Predicate<Object> or(Predicate<? super Object> other) {
			return other;
		}
	};

	private static Predicate<?> ALWAYS_TRUE = new Predicate<Object>() {
		@Override
		public boolean test(Object o) {
			return true;
		}

		@Override
		public Predicate<Object> and(Predicate<? super Object> other) {
			return other;
		}

		@Override
		public Predicate<Object> negate() {
			return (Predicate<Object>) ALWAYS_TRUE;
		}

		@Override
		public Predicate<Object> or(Predicate<? super Object> other) {
			return this;
		}
	};

	public static <T> Predicate<T> alwaysFalse() {
		return (Predicate<T>) ALWAYS_FALSE;
	}

	public static <T> Predicate<T> alwaysTrue() {
		return (Predicate<T>) ALWAYS_TRUE;
	}

}
