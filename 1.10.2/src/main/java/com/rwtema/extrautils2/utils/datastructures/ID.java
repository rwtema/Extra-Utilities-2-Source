package com.rwtema.extrautils2.utils.datastructures;

public final class ID<T> {
	public final T object;
	private final int hash;

	public ID(T object) {
		this.object = object;
		hash = System.identityHashCode(object);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		return other != null &&
				other.getClass() == ID.class &&
				hash == other.hashCode() &&
				object == ((ID) other).object;
	}
}
