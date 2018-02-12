package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.datastructures.FunctionABBool;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TCharArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class CollectionHelper {


	public static final char[] CHAR_DIGITS;
	public static final String[] STRING_DIGITS;

	public static HashingStrategy<ItemStack> HASHING_STRATEGY_ITEMSTACK_ITEM = new HashingStrategy<ItemStack>() {
		@Override
		public int computeHashCode(ItemStack object) {
			if (StackHelper.isNull(object) || object.getItem() == null) return 0;
			return object.getItem().hashCode();
		}

		@Override
		public boolean equals(ItemStack o1, ItemStack o2) {
			return o1 == o2 || StackHelper.isNonNull(o1) && StackHelper.isNonNull(o2) && o1.getItem() == o2.getItem();
		}
	};
	public static HashingStrategy<ItemStack> HASHING_STRATEGY_ITEMSTACK = new HashingStrategy<ItemStack>() {
		@Override
		public int computeHashCode(ItemStack object) {
			if (StackHelper.isNull(object) || object.getItem() == null) return 0;
			return object.getItem().hashCode() * 31 + object.getItemDamage();
		}

		@Override
		public boolean equals(ItemStack o1, ItemStack o2) {
			return o1 == o2 || StackHelper.isNonNull(o1) && StackHelper.isNonNull(o2) && o1.getItem() == o2.getItem() && o1.getItemDamage() == o2.getItemDamage();
		}
	};

	static {
		TCharArrayList chars = new TCharArrayList(10 + 26 * 2);
		for (int i = 0; i < 10; i++) {
			chars.add((char) ('0' + i));
		}

		for (int i = 0; i < 26; i++) {
			chars.add((char) ('a' + i));
		}

		for (int i = 0; i < 26; i++) {
			chars.add((char) ('A' + i));
		}

		chars.addAll(new char[]{'_', '-', '=', '+', '%', '@', '!', '?', '*', '^', '$', '&', '#', '~', ':', ';'});

		CHAR_DIGITS = chars.toArray();
		STRING_DIGITS = new String[CHAR_DIGITS.length];
		for (int i = 0; i < STRING_DIGITS.length; i++) {
			STRING_DIGITS[i] = String.valueOf(CHAR_DIGITS[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, V> HashMap<K, V> newHashMap(Object... objects) {
		HashMap<K, V> map = new HashMap<>();
		for (int i = 0; i < objects.length; i += 2) {
			map.put((K) objects[i], (V) objects[i + 1]);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> ImmutableMap<K, V> newConstMap(Object... objects) {
		ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		for (int i = 0; i < objects.length; i += 2) {
			builder.put((K) objects[i], (V) objects[i + 1]);
		}

		return builder.build();
	}

	public static <T> Iterable<T> removeNulls(final Iterable<T> iterable) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					Iterator<T> iterator = iterable.iterator();

					T next;

					@Override
					public boolean hasNext() {
						while (next == null) {
							if (!iterator.hasNext()) return false;
							next = iterator.next();
						}
						return true;
					}

					@Override
					public T next() {
						return next;
					}

					@Override
					public void remove() {

					}
				};
			}
		};
	}


	@SuppressWarnings("unchecked")
	public static <K, V, T extends Map<K, V>> T populateMap(T map, Object... objects) {
		for (int i = 0; i < objects.length; i += 2) {
			map.put((K) objects[i], (V) objects[i + 1]);
		}
		return map;
	}


	public static <K extends Enum<K>, V> EnumMap<K, V> populateEnumMap(Class<K> clazz, Function<K, V> function) {
		EnumMap<K, V> map = new EnumMap<>(clazz);
		for (K k : clazz.getEnumConstants()) {
			map.put(k, function.apply(k));
		}
		return map;
	}

	public static <K extends Enum<K>> EnumMap<K, EnumSet<K>> populateEnumMultiMap(Class<K> clazz, FunctionABBool<K, K> function) {
		EnumMap<K, EnumSet<K>> map = new EnumMap<>(clazz);
		for (K input : clazz.getEnumConstants()) {
			EnumSet<K> set = EnumSet.noneOf(clazz);
			for (K k2 : clazz.getEnumConstants()) {
				if (function.apply(input, k2)) {
					set.add(k2);
				}
			}
			map.put(input, set);
		}
		return map;
	}

	public static <K> K getRandomElementArray(K[] ks) {
		int i = XURandom.rand.nextInt(ks.length);
		return ks[i];
	}

	@SafeVarargs
	public static <K> K getRandomElementMulti(K a, K... ks) {
		int i = XURandom.rand.nextInt(ks.length + 1);
		if (i == 0) return a;
		return ks[i - 1];
	}

	public static <K> K getRandomElement(Iterable<K> iterable) {
		return getRandomElement(iterable, XURandom.rand);
	}

	public static <K> K getRandomElement(Iterable<K> iterable, Random rand) {
		K r = null;
		int t = 0;

		for (K k : iterable) {
			t++;
			if (t == 1 || rand.nextInt(t) == 0) {
				r = k;
			}
		}
		return r;
	}

	public static <T> Iterable<ObjectFloatEntry<T>> iterateTOF(TObjectFloatMap<T> v) {
		return () -> new Iterator<ObjectFloatEntry<T>>() {
			final TObjectFloatIterator<T> iterator = v.iterator();
			ObjectFloatEntry<T> entry = new ObjectFloatEntry<T>() {
				@Override
				public T getKey() {
					return iterator.key();
				}

				@Override
				public float getValue() {
					return iterator.value();
				}
			};

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public ObjectFloatEntry<T> next() {
				iterator.advance();
				return entry;
			}
		};
	}

	public static <T> Iterable<ObjectIntEntry<T>> iterateTIO(TIntObjectMap<T> v) {
		return () -> new Iterator<ObjectIntEntry<T>>() {
			final TIntObjectIterator<T> iterator = v.iterator();
			ObjectIntEntry<T> entry = new ObjectIntEntry<T>() {

				@Override
				public T getObject() {
					return iterator.value();
				}

				@Override
				public int getInt() {
					return iterator.key();
				}
			};

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public ObjectIntEntry<T> next() {
				iterator.advance();
				return entry;
			}
		};
	}

	public static <T> Iterable<ObjectIntEntry<T>> iterateTOI(TObjectIntMap<T> v) {
		return () -> new Iterator<ObjectIntEntry<T>>() {
			final TObjectIntIterator<T> iterator = v.iterator();
			ObjectIntEntry<T> entry = new ObjectIntEntry<T>() {

				@Override
				public T getObject() {
					return iterator.key();
				}

				@Override
				public int getInt() {
					return iterator.value();
				}
			};

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public ObjectIntEntry<T> next() {
				iterator.advance();
				return entry;
			}
		};
	}

	public static <T> Iterable<T> wrapConcurrentErrorReport(Iterable<T> iterable) {
		final Iterator<T> iterator = iterable.iterator();
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					T curValue;

					@Nullable
					T prevValue;

					@Override
					public boolean hasNext() {
						try {
							return iterator.hasNext();
						} catch (ConcurrentModificationException err) {
							if (prevValue == null) throw err;
							throw new ConcurrentModificationException("Comodification error\n Last returned value:" + prevValue + (prevValue != null ? (" (" + prevValue.getClass() + ")") : ""), err);
						}
					}

					@Override
					public T next() {
						try {
							curValue = iterator.next();
						} catch (ConcurrentModificationException err) {
							if (prevValue == null) throw err;
							throw new ConcurrentModificationException("Comodification error\n Last returned value:" + prevValue + (prevValue != null ? (" (" + prevValue.getClass() + ")") : ""), err);
						}

						return curValue;
					}
				};
			}
		};
	}

	public static <T> void performConcurrentErrorReporting(Iterable<T> iterable, Consumer<T> consumer) {
		Iterator<T> iterator = iterable.iterator();
		T prevValue = null;
		T curValue;
		while (iterator.hasNext()) {
			try {
				curValue = iterator.next();
			} catch (ConcurrentModificationException err) {
				if (prevValue == null) throw err;
				throw new ConcurrentModificationException("Comodification error\n Last returned value:" + prevValue + " (" + prevValue.getClass() + ")", err);
			}

			prevValue = curValue;

			consumer.accept(curValue);
		}
	}



	public interface ObjectFloatEntry<T> {
		T getKey();

		float getValue();

		default ObjectFloatEntry<T> toImmutable() {
			T key = getKey();
			float value = getValue();
			return new ObjectFloatEntry<T>() {
				@Override
				public T getKey() {
					return key;
				}

				@Override
				public float getValue() {
					return value;
				}
			};
		}
	}


	public interface ObjectIntEntry<T> {
		T getObject();

		int getInt();

		default ObjectIntEntry<T> toImmutable() {
			T key = getObject();
			int value = getInt();
			return new ObjectIntEntry<T>() {
				@Override
				public T getObject() {
					return key;
				}

				@Override
				public int getInt() {
					return value;
				}
			};
		}
	}

	public static <K,V> Map<K,V> createMap(Collection<K> collection, Function<K,V> function){
		ImmutableMap.Builder<K, V> builder = ImmutableMap.<K, V>builder();
		for (K k : collection) {
			V apply = function.apply(k);
			if(apply != null) {
				builder.put(k, apply);
			}
		}
		return builder.build();
	}

	public static <K extends Enum<K>, V> EnumMap<K,V> createMap(Class<K> enumClazz, Function<K,V> function){
		EnumMap<K, V> map = new EnumMap<>(enumClazz);
		for (K k : enumClazz.getEnumConstants()) {
			V v = function.apply(k);
			if(v != null){
				map.put(k, v);
			}
		}
		return map;
	}


	@SafeVarargs
	public static <T> ImmutableList<Pair<T, T>> splitInputListofPairs(T... inputs){
		ImmutableList.Builder<Pair<T, T>> builder = ImmutableList.builder();
		for (int i = 0; i < inputs.length; i+=2) {
			builder.add(Pair.of(inputs[i], inputs[i+1]));
		}
		return builder.build();
	}


	public static <T> Pair<ImmutableList<T>, ImmutableList<T>> splitInputPairsofList(T[] inputs){
		ImmutableList.Builder<T> builder_a = ImmutableList.builder();
		ImmutableList.Builder<T> builder_b = ImmutableList.builder();
		for (int i = 0; i < inputs.length; i+=2) {
			builder_a.add(inputs[i]);
			builder_b.add(inputs[i+1]);
		}
		return Pair.of(builder_a.build(), builder_b.build());
	}
}
