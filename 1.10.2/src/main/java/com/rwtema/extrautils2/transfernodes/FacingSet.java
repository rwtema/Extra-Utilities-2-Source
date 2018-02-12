package com.rwtema.extrautils2.transfernodes;

import com.google.common.collect.ImmutableList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.util.EnumFacing;

@SuppressWarnings("unchecked")
public class FacingSet extends AbstractSet<EnumFacing> implements Set<EnumFacing> {
	private static Collection<EnumFacing>[] facings;

	static {
		facings = new Collection[64];
		for (int i = 0; i < 64; i++) {
			ArrayList<EnumFacing> list = new ArrayList<>();
			for (EnumFacing facing : EnumFacing.values()) {
				if ((i & (1 << facing.ordinal())) != 0)
					list.add(facing);
			}

			facings[i] = ImmutableList.copyOf(list);
		}
	}

	int mask;

	@Override
	public int size() {
		return facings[mask].size();
	}

	@Override
	public boolean isEmpty() {
		return mask == 0;
	}

	@Override
	public boolean contains(Object o) {
		EnumFacing facing = (EnumFacing) o;
		return (mask & (1 << facing.ordinal())) != 0;
	}

	@Nonnull
	@Override
	public Iterator<EnumFacing> iterator() {
		return facings[mask].iterator();
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return facings[mask].toArray();
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	@Nonnull
	@Override
	public <T> T[] toArray(@Nonnull T[] a) {
		return facings[mask].toArray(a);
	}

	@Override
	public boolean add(EnumFacing facing) {
		if ((mask & 1 << facing.ordinal()) == 0) {
			mask |= 1 << facing.ordinal();
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(Object o) {
		EnumFacing facing = (EnumFacing) o;

		if ((mask & 1 << facing.ordinal()) != 0) {
			mask &= ~(1 << facing.ordinal());
			return true;
		}

		return false;
	}

	@Override
	public void clear() {
		mask = 0;
	}
}
