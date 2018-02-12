package com.rwtema.extrautils2.api.machine;

import net.minecraft.item.ItemStack;

public class MachineSlotItem extends MachineSlot<ItemStack> {

	public final String backgroundTexture;

	public MachineSlotItem(String name) {
		this(name, 64);
	}

	public MachineSlotItem(String name, int stackCapacity) {
		this(name, false, stackCapacity);
	}

	public MachineSlotItem(String name, boolean optional, int stackCapacity) {
		this(name, 0xffffffff, optional, null, stackCapacity);
	}

	public MachineSlotItem(String name, int color, boolean optional, String backgroundTexture, int stackCapacity) {
		super(name, color, optional, stackCapacity);
		this.backgroundTexture = backgroundTexture;
	}

	@Override
	public String toString() {
		return "SlotItem{" + name + "}";
	}
}
