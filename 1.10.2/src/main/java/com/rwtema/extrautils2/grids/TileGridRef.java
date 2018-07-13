package com.rwtema.extrautils2.grids;

import gnu.trove.set.hash.THashSet;

import java.lang.ref.WeakReference;

public final class TileGridRef<T extends XUTileGrid> extends WeakReference<T> {
	final int hash;
	public THashSet<Grid> grids = new THashSet<>(2);

	public TileGridRef(T referent) {
		super(referent, GridHandler.gridQueue);
		hash = System.identityHashCode(referent);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;

		Object t = get();
		if (t == null) return false;
		if (getClass() != o.getClass()) return t == o;

		TileGridRef<?> tileGridRef = (TileGridRef<?>) o;
		return hash == tileGridRef.hash && t == tileGridRef.get();

	}

	@Override
	public int hashCode() {
		return hash;
	}
}
