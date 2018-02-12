package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class BinaryOperatorCollector<T> implements Collector<T, MutableReference<T>, T> {
	final T initialValue;
	final BinaryOperator<T> combine;

	public BinaryOperatorCollector(@Nullable T initialValue, BinaryOperator<T> combine) {
		this.initialValue = initialValue;
		this.combine = combine;
	}

	@Override
	public Supplier<MutableReference<T>> supplier() {
		return () -> new MutableReference.Impl<T>(initialValue);
	}

	@Override
	public BiConsumer<MutableReference<T>, T> accumulator() {
		return (holder, t) -> {
			T val = holder.get();
			if (val == null) {
				holder.set(t);
			} else {
				holder.set(combine.apply(val, t));
			}
		};
	}

	@Override
	public BinaryOperator<MutableReference<T>> combiner() {
		return (holder, holder2) -> {
			if (holder.get() == null) return holder2;
			if (holder2.get() == null) return holder;
			return new MutableReference.Impl<T>(combine.apply(holder.get(), holder2.get()));
		};
	}

	@Override
	public Function<MutableReference<T>, T> finisher() {
		return MutableReference::get;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return ImmutableSet.of(Characteristics.UNORDERED);
	}

}
