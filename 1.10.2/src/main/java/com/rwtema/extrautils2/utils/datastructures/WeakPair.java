package com.rwtema.extrautils2.utils.datastructures;

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class WeakPair<L, R> {
	private WeakReference<L> left;
	private WeakReference<R> right;

	public WeakPair(L left, R right) {
		this.left = new WeakReference<>(left);
		this.right = new WeakReference<>(right);
	}

	@Nullable
	public L getLeft() {
		WeakReference<L> curLeft = this.left;
		if (curLeft == null) return null;
		L l = curLeft.get();
		WeakReference<R> right;
		if (l == null || (right = this.right) == null || right.get() == null) {
			this.left = null;
			this.right = null;
			return null;
		}
		return l;
	}

	@Nullable
	public R getRight() {
		WeakReference<R> curRight = this.right;
		if (curRight == null) return null;
		R r = curRight.get();
		WeakReference<L> curLeft;
		if (r == null || (curLeft = this.left) == null || curLeft.get() == null) {
			this.left = null;
			this.right = null;
			return null;
		}
		return r;
	}

	public Pair<L, R> getPair() {
		WeakReference<L> curLeft = this.left;
		WeakReference<R> curRight = this.right;
		if (curLeft == null || curRight == null) return null;
		L l = curLeft.get();
		R r = curRight.get();
		if (l == null || r == null) {
			this.left = null;
			this.right = null;
			return null;
		}
		return Pair.of(l, r);
	}
}
