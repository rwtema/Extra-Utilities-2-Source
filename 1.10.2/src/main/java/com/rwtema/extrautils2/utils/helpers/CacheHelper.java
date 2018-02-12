package com.rwtema.extrautils2.utils.helpers;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class CacheHelper {
	public <K, V> Cache<K, V> newSimpleCache(Function<K, V> function) {
		return CacheBuilder.newBuilder().build(
				CacheLoader.from(function));
	}


	public <K, V> Cache<K, V> weakCache(Function<K, V> function) {
		return CacheBuilder.newBuilder().weakKeys().build(
				CacheLoader.from(function));
	}
}
