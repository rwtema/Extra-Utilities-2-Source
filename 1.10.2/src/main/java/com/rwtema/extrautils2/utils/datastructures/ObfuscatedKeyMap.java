package com.rwtema.extrautils2.utils.datastructures;

import gnu.trove.map.hash.TIntObjectHashMap;

import javax.annotation.Nullable;
import java.util.Map;

public class ObfuscatedKeyMap<V> {
	private final  int seed, max_search;
	private final TIntObjectHashMap<SubEntry<V>> map;

	public ObfuscatedKeyMap(int seed, int max_search, TIntObjectHashMap<SubEntry<V>> map) {
		this.seed = seed;
		this.max_search = max_search;
		this.map = map;
	}

	public static <V> ObfuscatedKeyMap<V> compile(Map<String, V> map, int seed) {
		TIntObjectHashMap<SubEntry<V>> hashMap = new TIntObjectHashMap<>();
		int max_len = 1;
		if (map.containsKey("")) {
			hashMap.put(0, new SubEntry<>(map.get(""), (byte) 0, 0));
		}

		for (Map.Entry<String, V> entry : map.entrySet()) {
			String key = entry.getKey();
			int potentialHash = seed;
			int p = 0, i = 0;
			while (true) {
				potentialHash = potentialHash * 63 + p * 31 + key.charAt(i);
				if (!hashMap.containsKey(potentialHash)) {
					hashMap.put(potentialHash, new SubEntry<>(entry.getKey(), entry.getValue()));
					break;
				}
				i++;
				if (i >= key.length()) i = 0;
				p++;
			}
			max_len = Math.max(max_len, p + 1);
		}
		return new ObfuscatedKeyMap<>(seed, max_len, hashMap);
	}

	@Nullable
	public V get(String key) {
		if (key.isEmpty()) {
			SubEntry<V> subEntry = map.get(0);
			if (subEntry.length == 0 && subEntry.altHash == 0) {
				return subEntry.value;
			}
			return null;
		} else {
			int hash = key.hashCode();
			int potentialHash = seed;
			byte length = (byte) key.length();
			int p = 0, i = 0;
			while (p < max_search) {
				potentialHash = potentialHash * 63 + p * 31 + key.charAt(i);
				SubEntry<V> v = map.get(potentialHash);
				if (v == null) return null;
				if (length == v.length && hash == v.altHash) {
					return v.value;
				}
				i++;
				if (i >= key.length()) i = 0;
				p++;
			}
			return null;
		}
	}

	private static class SubEntry<T> {
		T value;
		byte length;
		int altHash;

		public SubEntry(String key, T value) {
			this(value, (byte) key.length(), key.hashCode());
		}

		public SubEntry(T value, byte length, int altHash) {
			this.value = value;
			this.length = length;
			this.altHash = altHash;
		}
	}
}
