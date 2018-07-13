package com.rwtema.extrautils2.utils.datastructures;

import javax.annotation.Nonnull;
import java.util.*;

public class WeakSet<E> extends AbstractSet<E> implements Set<E> {
	private static final Object PRESENT = new Object();
	private final transient Map<E, Object> map;

	public WeakSet() {
		this(new WeakHashMap<>());
	}

	public WeakSet(WeakHashMap<E, Object> baseMap) {
		this.map = baseMap;
	}

	@Nonnull
	@SuppressWarnings("NullableProblems")
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public boolean add(E e) {
		return map.put(e, PRESENT) == null;
	}

	public boolean remove(Object o) {
		return map.remove(o) == PRESENT;
	}

	public void clear() {
		map.clear();
	}
}
