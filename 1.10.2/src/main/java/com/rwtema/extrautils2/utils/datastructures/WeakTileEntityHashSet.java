package com.rwtema.extrautils2.utils.datastructures;

import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.datastructures.WeakSet;
import java.util.Iterator;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import net.minecraft.tileentity.TileEntity;

public class WeakTileEntityHashSet<T> extends WeakSet<T> {
	public WeakTileEntityHashSet() {
		super();
	}

	public WeakTileEntityHashSet(WeakHashMap<T, Object> baseMap) {
		super(baseMap);
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		final Iterator<T> base = super.iterator();
		return new Iterator<T>() {
			T next;

			@Override
			public boolean hasNext() {
				while (true) {
					if (!base.hasNext()) return false;
					next = base.next();
					if (next != null && XUTile.isLoaded((TileEntity) next)) {
						return true;
					}
				}
			}

			@Override
			public T next() {
				return next;
			}

			@Override
			public void remove() {
				base.remove();
			}
		};
	}
}
