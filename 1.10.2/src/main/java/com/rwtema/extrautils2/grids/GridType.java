package com.rwtema.extrautils2.grids;

public abstract class GridType {
	public abstract Grid createGrid();


	public Grid mergeGrids(Grid a, Grid b) {
		if (a.refList.size() < b.refList.size()) {
			Grid t = b;
			b = a;
			a = t;
		}

		b.isValid = false;
		for (TileGridRef<XUTileGrid> refs : b.refList) {
			refs.grids.remove(b);
			a.refList.add(refs);
			refs.grids.add(a);
		}

		a.onMerge();
		return a;
	}
}
