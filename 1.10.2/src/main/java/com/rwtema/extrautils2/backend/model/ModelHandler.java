package com.rwtema.extrautils2.backend.model;

import net.minecraftforge.common.MinecraftForge;

public class ModelHandler {

	public static ModelHandler instance = new ModelHandler();

	public static void init() {
		MinecraftForge.EVENT_BUS.register(instance);
	}


}
