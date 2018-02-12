package com.rwtema.extrautils2.utils.datastructures;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class IntArrKey {
	public final int[] val;
	private final int hash;

	public IntArrKey(@Nonnull int... val) {
		this.val = val;
		hash = Arrays.hashCode(val);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntArrKey intArrKey = (IntArrKey) o;

		return Arrays.equals(val, intArrKey.val);
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
