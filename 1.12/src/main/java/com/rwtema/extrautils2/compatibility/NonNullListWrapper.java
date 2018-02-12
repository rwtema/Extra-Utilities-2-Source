package com.rwtema.extrautils2.compatibility;

import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

public class NonNullListWrapper<E> extends NonNullList<E> {
	public NonNullListWrapper(List<E> delegateIn, @Nullable E listType) {
		super(delegateIn, listType);
	}

	public NonNullListWrapper(List<E> list) {
		this(list, null);
	}

	public static <E> NonNullList<E> wrap(List<E> subItems) {
		if (subItems instanceof NonNullList) return ((NonNullList<E>) subItems);
		return new NonNullListWrapper<E>(subItems, null);
	}
}
