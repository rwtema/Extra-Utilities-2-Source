package com.rwtema.extrautils2.utils.datastructures;

import com.rwtema.extrautils2.utils.XURandom;

import java.util.ArrayList;
import java.util.Iterator;

public class ListRandomOffset<E> implements Iterable<E> {
	final ArrayList<E> list;

	public ListRandomOffset(ArrayList<E> list) {
		this.list = list;
	}

	@Override
	public Iterator<E> iterator() {
		if (list.size() <= 1) return list.iterator();

		return new Iterator<E>() {
			int cursor = 0;
			int offset = XURandom.rand.nextInt(list.size());

			@Override
			public boolean hasNext() {
				return cursor < list.size();
			}

			@Override
			public E next() {
				int i = (cursor + offset) % list.size();
				cursor++;
				return list.get(i);
			}
		};
	}
}
