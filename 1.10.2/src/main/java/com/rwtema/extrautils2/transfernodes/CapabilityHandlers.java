package com.rwtema.extrautils2.transfernodes;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;

public class CapabilityHandlers {
	public static final ArrayList<Capability<?>> capabilities = new ArrayList<>();

	static {
		capabilities.add(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}
}
