package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.List;

public abstract class ArrayAccess<T> implements Iterable<T> {

	public abstract void set(int i, T t);

	public abstract T get(int i);

	public abstract int length();


	public static class WrapArray<T> extends ArrayAccess<T> {
		final T[] array;

		public WrapArray(T[] array) {
			this.array = array;
		}

		@Override
		public void set(int i, T t) {
			array[i] = t;
		}

		@Override
		public T get(int i) {
			return array[i];
		}

		@Override
		public int length() {
			return array.length;
		}

		@Override
		public Iterator<T> iterator() {
			return Iterators.forArray(array);
		}
	}

	public static class WrapList<T> extends ArrayAccess<T> {
		final List<T> list;

		public WrapList(List<T> list) {
			this.list = list;
		}

		@Override
		public void set(int i, T t) {
			list.set(i, t);
		}

		@Override
		public T get(int i) {
			return list.get(i);
		}

		@Override
		public int length() {
			return list.size();
		}

		@Override
		public Iterator<T> iterator() {
			return list.iterator();
		}
	}
}
