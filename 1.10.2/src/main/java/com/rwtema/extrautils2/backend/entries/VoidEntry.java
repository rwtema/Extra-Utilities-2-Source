package com.rwtema.extrautils2.backend.entries;

public class VoidEntry extends Entry<Void> {
	public VoidEntry(String name) {
		super(name);
	}

	@Override
	public Void initValue() {
		return null;
	}
}
