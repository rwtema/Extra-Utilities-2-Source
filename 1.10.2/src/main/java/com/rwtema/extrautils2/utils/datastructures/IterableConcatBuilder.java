package com.rwtema.extrautils2.utils.datastructures;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IterableConcatBuilder<E> {
	List<E> backupIterable = null;

	List<Iterable<? extends E>> iterables = new ArrayList<>();

	private List<E> initBackup() {
		if (backupIterable == null) {
			this.backupIterable = new ArrayList<>();
			this.iterables.add(backupIterable);
		}
		return backupIterable;
	}

	public IterableConcatBuilder<E> add(E element) {
		initBackup().add(element);
		return this;
	}


	public IterableConcatBuilder<E> addAll(Iterable<? extends E> elements) {
		iterables.add(elements);
		return this;
	}


	public IterableConcatBuilder<E> addArray(E... elements) {
		Collections.addAll(initBackup(), elements);
		return this;
	}

	public IterableConcatBuilder<E> addAll(Iterator<? extends E> elements) {
		iterables.add(Lists.newArrayList(elements));
		return this;
	}

	public IterableConcatBuilder<E> combine(IterableConcatBuilder<E> other) {
		if (other.backupIterable != null) {
			if (backupIterable == null) {
				backupIterable = other.backupIterable;
			} else {
				backupIterable.addAll(other.backupIterable);
			}
		}
		this.iterables.addAll(other.iterables);
		return this;
	}

	public Iterable<E> build() {
		return Iterables.concat(iterables);
	}
}
