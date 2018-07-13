package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class XUCollectorHelper {
	public static <E> Collector<E, ?, List<Pair<E, Integer>>> runs() {
		return Collector.<E, LinkedList<MutablePair<E, Integer>>, List<Pair<E, Integer>>>of(
				LinkedList::new,
				(pairs, e) -> {
					Pair<E, Integer> lastValue;
					if (!pairs.isEmpty() && Objects.equals(e, (lastValue = pairs.peekLast()).getKey())) {
						lastValue.setValue(lastValue.getValue() + 1);
					} else {
						pairs.add(new MutablePair<>(e, 1));
					}
				}, (pairs, pairs2) -> {
					if (pairs.isEmpty()) return pairs2;
					if (pairs2.isEmpty()) return pairs;
					Pair<E, Integer> last = pairs.peekLast();
					Pair<E, Integer> first = pairs2.peekFirst();
					if (Objects.equals(last.getValue(), first.getValue())) {
						first = pairs2.pollFirst();
						last.setValue(last.getValue() + first.getValue());
					}
					pairs.addAll(pairs2);
					return pairs;
				}, pairs -> pairs.stream().map(t -> Pair.of(t.getLeft(), t.getRight())).collect(Collectors.toList())
		);
	}

	public static <E extends Enum<E>> Collector<E, ?, EnumMap<E, Integer>> tabulate(Class<E> clazz) {
		return Collector.of(
				() -> new int[clazz.getEnumConstants().length],
				(array, e) -> array[e.ordinal()]++,
				(array1, array2) -> {
					for (int i = 0; i < array2.length; i++) {
						array1[i] += array2[i];
					}
					return array1;
				},
				array -> {
					EnumMap<E, Integer> map = new EnumMap<>(clazz);
					for (int i = 0; i < array.length; i++) {
						map.put(clazz.getEnumConstants()[i], array[i]);
					}
					return map;
				}
		);
	}

	public static <V> Collector<V, ?, TObjectIntHashMap<V>> tabulate() {
		return Collector.of(
				TObjectIntHashMap::new,
				(map, v) -> map.adjustOrPutValue(v, 1, 1),
				(mapA, mapB) -> {
					mapA.forEachEntry((a, b) -> {
						mapB.adjustOrPutValue(a, b, b);
						return true;
					});
					return mapA;
				}
		);
	}


	public static <T, K, V> Collector<T, ?, HashMap<K, V>> toHashMap(Function<T, K> getKey, Function<T, V> getValue) {
		return Collector.of(
				HashMap::new,
				(kvHashMap, t) -> kvHashMap.put(getKey.apply(t), getValue.apply(t)),
				(kvHashMap, kvHashMap2) -> {
					kvHashMap.putAll(kvHashMap2);
					return kvHashMap;
				}
		);
	}

	public static <K, V> Collector<K, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> makeMap(Function<K, V> function) {
		return Collector.of(
				ImmutableMap::builder,
				(kvBuilder, k) -> kvBuilder.put(k, function.apply(k)),
				(kvBuilder, kvBuilder2) -> kvBuilder.putAll(kvBuilder2.build()),
				ImmutableMap.Builder::build
		);
	}
}
