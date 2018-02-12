package com.rwtema.extrautils2.utils.helpers;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;

public class WeakHelper {
	private static final ReferenceQueue<Object> queue = new ReferenceQueue<>();


	public static <T> Iterable<CollectionHelper.ObjectIntEntry<T>> wrapObjectFloat(Iterable<CollectionHelper.ObjectIntEntry<WeakReference<T>>> iterable) {
		return () -> {
			Iterator<CollectionHelper.ObjectIntEntry<WeakReference<T>>> iterator = iterable.iterator();
			return new Iterator<CollectionHelper.ObjectIntEntry<T>>() {
				T nextObject;
				int nextInt;
				CollectionHelper.ObjectIntEntry<T> entry = new CollectionHelper.ObjectIntEntry<T>() {
					@Override
					public T getObject() {
						return nextObject;
					}

					@Override
					public int getInt() {
						return nextInt;
					}
				};

				@Override
				public boolean hasNext() {
					while (iterator.hasNext()) {
						CollectionHelper.ObjectIntEntry<WeakReference<T>> next = iterator.next();
						nextObject = next.getObject().get();
						if (nextObject != null) {
							nextInt = next.getInt();
							return true;
						}
					}
					return false;
				}

				@Override
				public CollectionHelper.ObjectIntEntry<T> next() {
					return entry;
				}
			};
		};
	}


	public static <T> Iterable<CollectionHelper.ObjectFloatEntry<T>> wrapObjectInt(Iterable<CollectionHelper.ObjectFloatEntry<WeakReference<T>>> iterable) {
		return () -> {
			Iterator<CollectionHelper.ObjectFloatEntry<WeakReference<T>>> iterator = iterable.iterator();
			return new Iterator<CollectionHelper.ObjectFloatEntry<T>>() {
				T nextObject;
				float nextFloat;
				CollectionHelper.ObjectFloatEntry<T> entry = new CollectionHelper.ObjectFloatEntry<T>() {
					@Override
					public T getKey() {
						return nextObject;
					}

					@Override
					public float getValue() {
						return nextFloat;
					}
				};

				@Override
				public boolean hasNext() {
					while (iterator.hasNext()) {
						CollectionHelper.ObjectFloatEntry<WeakReference<T>> next = iterator.next();
						nextObject = next.getKey().get();
						if (nextObject != null) {
							nextFloat = next.getValue();
							return true;
						}
					}
					return false;
				}

				@Override
				public CollectionHelper.ObjectFloatEntry<T> next() {
					return entry;
				}
			};
		};
	}

	public static <T> Iterable<T> wrapWeakIterator(Iterable<WeakReference<T>> iterable, final boolean removeStaleEntries) {
		return () -> new Iterator<T>() {
			Iterator<WeakReference<T>> iterator = iterable.iterator();

			T next;

			@Override
			public boolean hasNext() {
				while (iterator.hasNext()) {
					next = iterator.next().get();
					if (next != null) {
						return true;
					} else if (removeStaleEntries) {
						iterator.remove();
					}
				}
				return false;
			}

			@Override
			public T next() {
				return next;
			}
		};
	}
}
