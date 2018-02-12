package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import java.util.*;
import javax.annotation.Nonnull;

public class ConcatList<T> extends AbstractList<T> {
	public final List<List<T>> subLists;
	public final List<T> modifiableList;

	public ConcatList() {
		subLists = new ArrayList<>();
		modifiableList = new ArrayList<>();
		subLists.add(modifiableList);
	}


	public void appendList(List<T> list) {
		subLists.add(list);
	}

	@Override
	public boolean add(T t) {
		return modifiableList.add(t);
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends T> c) {
		if (c instanceof List) {
			return subLists.add((List) c);
		} else
			return super.addAll(c);
	}

	@Override
	public T get(int index) {
		for (List<T> list : subLists) {
			int size = list.size();
			if (index < size) {
				return list.get(index);
			} else {
				index -= size;
			}
		}

		return null;
	}

	@Override
	public int size() {
		int size = 0;
		for (List<T> list : subLists) {
			size += list.size();
		}
		return size;
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return Iterables.concat(subLists).iterator();
	}


}
