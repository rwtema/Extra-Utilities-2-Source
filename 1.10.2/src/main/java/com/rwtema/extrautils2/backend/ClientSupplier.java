package com.rwtema.extrautils2.backend;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface ClientSupplier<T> extends Supplier<T> {
	@Override
	@Nullable
	default T get() {
		return null;
	}
}
