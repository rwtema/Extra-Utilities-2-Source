package com.rwtema.extrautils2.utils.datastructures;

import javax.annotation.Nonnull;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class ListConcat<E> extends AbstractList<E> {
	final List<List<E>> subLists;


	public ListConcat(List<List<E>> subLists) {
		this.subLists = subLists;
	}

	@Override
	public E get(int index) {
		for (int i = 0; i < 2; i++) {
			for (List<E> subList : subLists) {
				int size = subList.size();
				if (index < size) {
					return subList.get(index);
				} else {
					index -= size;
				}
			}
		}
		return null;
	}

	@Override
	public int size() {
		return subLists.stream().mapToInt(List::size).sum();
	}

	@Nonnull
	@Override
	public Iterator<E> iterator() {
		return subLists.stream().flatMap(List::stream).iterator();
	}
}
