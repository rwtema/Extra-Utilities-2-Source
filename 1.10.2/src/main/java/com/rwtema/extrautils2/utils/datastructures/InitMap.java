package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.ForwardingMap;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class InitMap<K, V> extends ForwardingMap<K, V> {

	final Map<K, V> base;

	@SuppressWarnings("unchecked")
	public InitMap(Map base) {
		this.base = base;
	}

	public InitMap() {
		this(new WeakHashMap<K, V>());
	}

	@Override
	protected Map<K, V> delegate() {
		return base;
	}

	protected abstract V initValue(K key);

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		V v = super.get(key);
		if (v == null) {
			K k = (K) key;
			v = initValue(k);
			put(k, v);
		}
		return v;
	}
}
