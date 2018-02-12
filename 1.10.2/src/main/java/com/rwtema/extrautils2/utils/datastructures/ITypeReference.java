package com.rwtema.extrautils2.utils.datastructures;

public interface ITypeReference<T, R extends ITypeReference<T, R>> {
	R set(T value);

	T get();
}
