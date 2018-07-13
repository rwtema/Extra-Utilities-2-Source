package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.ImmutableSet;

import java.util.AbstractMap;
import java.util.Set;

public class CompiledKeyMap<K, V> extends AbstractMap<K, V> {


	@Override
	public Set<Entry<K, V>> entrySet() {
		return ImmutableSet.of();
	}
}
