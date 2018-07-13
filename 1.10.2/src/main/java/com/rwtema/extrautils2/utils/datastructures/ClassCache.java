package com.rwtema.extrautils2.utils.datastructures;

import java.util.WeakHashMap;

public abstract class ClassCache<T> {
	WeakHashMap<Class, T> values = new WeakHashMap<>();

	public T getFromObject(Object object) {
		if (object == null) return nullObjectValue();
		return getClass(object.getClass());
	}

	public T getClass(Class clazz) {
		if (clazz == null) return nullClassValue();
		if (values.containsKey(clazz)) {
			return values.get(clazz);
		}
		T t = calc(clazz);
		values.put(clazz, t);
		return t;
	}

	public void clear() {
		values.clear();
	}

	protected abstract T calc(Class clazz);

	protected T nullClassValue() {
		throw new NullPointerException();
	}


	protected T nullObjectValue() {
		throw new NullPointerException();
	}
}
