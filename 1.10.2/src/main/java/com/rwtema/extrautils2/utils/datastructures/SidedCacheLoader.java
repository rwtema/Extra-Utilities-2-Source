package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.cache.CacheLoader;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ISidedFunction;
import javax.annotation.Nonnull;

public abstract class SidedCacheLoader<K, V> extends CacheLoader<K, V> implements ISidedFunction<K, V> {
	@Override
	public final V load(@Nonnull K key) throws Exception {
		return ExtraUtils2.proxy.apply(this, key);
	}
}
