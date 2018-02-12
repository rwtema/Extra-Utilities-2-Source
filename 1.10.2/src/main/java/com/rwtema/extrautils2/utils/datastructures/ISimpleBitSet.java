package com.rwtema.extrautils2.utils.datastructures;

public interface ISimpleBitSet {
	boolean get(int bitIndex);

	void flip(int bitIndex);

	void set(int bitIndex);

	void set(int bitIndex, boolean value);

	void clear(int bitIndex);

	boolean isEmpty();
}
