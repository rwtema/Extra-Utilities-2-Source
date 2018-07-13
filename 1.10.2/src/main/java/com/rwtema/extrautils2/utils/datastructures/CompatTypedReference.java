package com.rwtema.extrautils2.utils.datastructures;

import net.minecraft.item.Item;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CompatTypedReference<T, V extends CompatTypedReference<T, V>> {
	@Nullable
	T ref;

	public CompatTypedReference() {
		this(null);
	}

	public CompatTypedReference(@Nullable T ref) {
		this.ref = ref;
	}

	public V set(T value) {
		ref = value;
		return (V) this;
	}

	public T setAndReturnValue(T value) {
		ref = value;
		return value;
	}

	@Nullable
	public T get() {
		return ref;
	}

	@Nonnull
	public T getNonnull() {
		return Validate.notNull(ref);
	}

	public Optional<T> getOptional() {
		return Optional.ofNullable(ref);
	}

	public static class Test extends CompatTypedReference<Item, Test> {

	}
}
