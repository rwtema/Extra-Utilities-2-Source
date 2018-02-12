package com.rwtema.extrautils2.utils.datastructures;

public final class IntPair {
	private final static int n = 16;

	private static IntPair[][] cache = new IntPair[n + 1][n + 1];

	public final int x, y;

	private IntPair(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static IntPair of(int a, int b) {
		if (a <= n && b <= n && a >= 0 && b >= 0) {
			if (cache[a][b] == null) {
				cache[a][b] = new IntPair(a, b);
			}
			return cache[a][b];
		}
		return new IntPair(a, b);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntPair)) return false;

		IntPair intPair = (IntPair) o;
		return x == intPair.x && y == intPair.y;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		return result;
	}
}
