package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.ImmutableSet;
import gnu.trove.TFloatCollection;
import gnu.trove.function.TFloatFunction;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.procedure.TFloatProcedure;
import gnu.trove.procedure.TObjectFloatProcedure;
import gnu.trove.procedure.TObjectProcedure;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class BlankTObjectFloatMap<T> implements TObjectFloatMap<T> {
	private static final BlankTObjectFloatMap INSTANCE = new BlankTObjectFloatMap();

	public static <S> BlankTObjectFloatMap<S> getInstance() {
		return INSTANCE;
	}

	@Override
	public float getNoEntryValue() {
		return 0;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(float value) {
		return false;
	}

	@Override
	public float get(Object key) {
		return 0;
	}

	@Override
	public float put(T key, float value) {
		return 0;
	}

	@Override
	public float putIfAbsent(T key, float value) {
		return 0;
	}

	@Override
	public float remove(Object key) {
		return 0;
	}

	@Override
	public void putAll(Map<? extends T, ? extends Float> m) {

	}

	@Override
	public void putAll(TObjectFloatMap<? extends T> map) {

	}

	@Override
	public void clear() {

	}

	@Override
	public Set<T> keySet() {
		return ImmutableSet.of();
	}

	@Override
	public Object[] keys() {
		return new Object[0];
	}

	@Override
	public T[] keys(T[] array) {
		return (T[]) (new Object[0]);
	}

	@Override
	public TFloatCollection valueCollection() {
		return new TFloatCollection() {
			@Override
			public float getNoEntryValue() {
				return 0;
			}

			@Override
			public int size() {
				return 0;
			}

			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public boolean contains(float entry) {
				return false;
			}

			@Override
			public TFloatIterator iterator() {
				return null;
			}

			@Override
			public float[] toArray() {
				return new float[0];
			}

			@Override
			public float[] toArray(float[] dest) {
				return new float[0];
			}

			@Override
			public boolean add(float entry) {
				return false;
			}

			@Override
			public boolean remove(float entry) {
				return false;
			}

			@Override
			public boolean containsAll(Collection<?> collection) {
				return false;
			}

			@Override
			public boolean containsAll(TFloatCollection collection) {
				return false;
			}

			@Override
			public boolean containsAll(float[] array) {
				return false;
			}

			@Override
			public boolean addAll(Collection<? extends Float> collection) {
				return false;
			}

			@Override
			public boolean addAll(TFloatCollection collection) {
				return false;
			}

			@Override
			public boolean addAll(float[] array) {
				return false;
			}

			@Override
			public boolean retainAll(Collection<?> collection) {
				return false;
			}

			@Override
			public boolean retainAll(TFloatCollection collection) {
				return false;
			}

			@Override
			public boolean retainAll(float[] array) {
				return false;
			}

			@Override
			public boolean removeAll(Collection<?> collection) {
				return false;
			}

			@Override
			public boolean removeAll(TFloatCollection collection) {
				return false;
			}

			@Override
			public boolean removeAll(float[] array) {
				return false;
			}

			@Override
			public void clear() {

			}

			@Override
			public boolean forEach(TFloatProcedure procedure) {
				return false;
			}
		};
	}

	@Override
	public float[] values() {
		return new float[0];
	}

	@Override
	public float[] values(float[] array) {
		return new float[0];
	}

	@Override
	public TObjectFloatIterator<T> iterator() {
		return new TObjectFloatIterator<T>() {
			@Override
			public T key() {
				return null;
			}

			@Override
			public float value() {
				return 0;
			}

			@Override
			public float setValue(float val) {
				return 0;
			}

			@Override
			public void advance() {

			}

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public void remove() {

			}
		};
	}

	@Override
	public boolean increment(T key) {
		return false;
	}

	@Override
	public boolean adjustValue(T key, float amount) {
		return false;
	}

	@Override
	public float adjustOrPutValue(T key, float adjust_amount, float put_amount) {
		return 0;
	}

	@Override
	public boolean forEachKey(TObjectProcedure<? super T> procedure) {
		return false;
	}

	@Override
	public boolean forEachValue(TFloatProcedure procedure) {
		return false;
	}

	@Override
	public boolean forEachEntry(TObjectFloatProcedure<? super T> procedure) {
		return false;
	}

	@Override
	public void transformValues(TFloatFunction function) {

	}

	@Override
	public boolean retainEntries(TObjectFloatProcedure<? super T> procedure) {
		return false;
	}
}
