package com.rwtema.extrautils2.utils.datastructures;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class LazyBitSet {
	long calc, result;

	final IntPredicate predicate;

	public LazyBitSet(IntPredicate predicate) {
		this.predicate = predicate;
	}

	public boolean get(int bitIndex){
		long mask = 1L << bitIndex;
		if ((calc & mask) == 0) {
			calc |= mask;
			if (predicate.test(bitIndex)) {
				result |= mask;
			}else{
				result &= ~mask;
			}
		}
		return (result & mask) != 0;
	}

	public void invalidateAll(){
		calc = 0;
	}

	public void invalide(int bitIndex){
		calc &= (1 << bitIndex);
	}

	public static class EnumBits<T extends Enum<T>> extends LazyBitSet {

		public EnumBits(Class<T> clazz, Predicate<T> predicate) {
			super(t -> predicate.test(clazz.getEnumConstants()[t]));
		}

		public boolean get(T t){
			return get(t.ordinal());
		}
	}
}
