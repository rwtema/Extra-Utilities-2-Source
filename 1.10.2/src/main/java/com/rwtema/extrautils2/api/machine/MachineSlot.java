package com.rwtema.extrautils2.api.machine;

public abstract class MachineSlot<T> {
	public final String name;
	public final int color;
	public final boolean optional;
	public final int stackCapacity;


	protected MachineSlot(String name, int color, boolean optional, int stackCapacity) {
		this.name = name;
		this.color = color;
		this.optional = optional;
		this.stackCapacity = stackCapacity;
	}
}
