package com.rwtema.extrautils2.utils.datastructures;

import java.util.HashMap;

public class PairMap<K extends Comparable<K>, V> {
	HashMap<K, HashMap<K, V>> map = new HashMap<>();

	public V get(K a, K b) {
		if (a.compareTo(b) <= 0)
			return doGet(a, b);
		else
			return doGet(b, a);
	}

	private V doGet(K low, K high) {
		HashMap<K, V> subMap = map.get(low);
		if (subMap == null) return null;
		return subMap.get(high);
	}


	public V put(K a, K b, V val) {
		if (a.compareTo(b) <= 0)
			return doPut(a, b, val);
		else
			return doPut(b, a, val);
	}

	private V doPut(K low, K high, V val) {
		return map.computeIfAbsent(low, t -> new HashMap<>()).put(high, val);
	}
}
