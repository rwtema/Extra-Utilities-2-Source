package com.rwtema.extrautils2.utils.helpers;

import com.rwtema.extrautils2.ExtraUtils2;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class NullHelper {
	public static final Object dummy = null;

	@Nullable
	public static <T> T nullable(@Nullable T t) {
		return t;
	}

	public static boolean checkShouldBeNull(@Nullable Object obj) {
		return obj != dummy;
	}

	public static <T> Optional<T> deobfOptional(Supplier<T> supplier){
		return ExtraUtils2.deobf_folder ? Optional.of(supplier.get()) : Optional.empty();
	}

	public static <T, P> Optional<T> deobfOptional(Function<P, T> func, P parameter ){
		return ExtraUtils2.deobf_folder ? Optional.of(func.apply(parameter)) : Optional.empty();
	}

	public static <T, R> R ifelse(T t, Function<T, R> notNull, Supplier<R> isNull) {
		return t == null ? isNull.get() : notNull.apply(t);
	}
}
