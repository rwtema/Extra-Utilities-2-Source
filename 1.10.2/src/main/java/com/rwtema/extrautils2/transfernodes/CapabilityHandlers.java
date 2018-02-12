package com.rwtema.extrautils2.transfernodes;

import java.util.ArrayList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class CapabilityHandlers {
	public static final ArrayList<Capability<?>> capabilities = new ArrayList<>();

	static {
		capabilities.add(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}
}
