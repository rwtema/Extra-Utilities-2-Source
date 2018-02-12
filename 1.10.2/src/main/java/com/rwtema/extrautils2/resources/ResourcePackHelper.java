package com.rwtema.extrautils2.resources;

import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;

public class ResourcePackHelper {
	public static List<IResourcePack> packs;

	public static List<IResourcePack> getiResourcePacks() {
		List<IResourcePack> packs1 = packs;
		if (packs1 == null)
			packs1 = ObfuscationReflectionHelper.getPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), "resourcePackList");
		return packs1;
	}
}
