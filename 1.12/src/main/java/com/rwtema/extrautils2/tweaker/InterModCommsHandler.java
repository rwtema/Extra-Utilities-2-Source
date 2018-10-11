package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.ExtraUtils2;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;


@ZenRegister
@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "InterModCommsHandler")
public class InterModCommsHandler {


	public static boolean checkEnqueuing() {
		if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
			CraftTweakerAPI.logError("IMC Message is being sent too late.");
			return false;
		}
		return true;
	}

	@ZenMethod
	public static boolean sendMessageString(String mod, String key, String message) {
		return checkEnqueuing() && FMLInterModComms.sendMessage(mod, key, message);
	}

	@ZenMethod
	public static boolean sendMessageItemStack(String mod, String key, IItemStack stack) {
		return checkEnqueuing() && FMLInterModComms.sendMessage(mod, key, (ItemStack) stack.getInternal());
	}

	@ZenMethod
	public static boolean sendMessageNBT(String mod, String key, DataMap dataMap) {
		NBTTagCompound nbtData = (NBTTagCompound) NBTConverter.from(dataMap);
		return checkEnqueuing() && FMLInterModComms.sendMessage(mod, key, nbtData);
	}

	@ZenMethod
	public static boolean sendMessageResourceLocation(String mod, String key, String resourceLocation) {
		return checkEnqueuing() && FMLInterModComms.sendMessage(mod, key, new ResourceLocation(resourceLocation));
	}


	@ZenMethod
	public static void sendRuntimeMessageString(String mod, String key, String message) {
		FMLInterModComms.sendRuntimeMessage(ExtraUtils2.instance, mod, key, message);
	}

	@ZenMethod
	public static void sendRuntimeMessageItemStack(String mod, String key, IItemStack stack) {
		FMLInterModComms.sendRuntimeMessage(ExtraUtils2.instance, mod, key, (ItemStack) stack.getInternal());
	}

	@ZenMethod
	public static void sendRuntimeMessageNBT(String mod, String key, DataMap dataMap) {
		NBTTagCompound nbtData = (NBTTagCompound) NBTConverter.from(dataMap);
		FMLInterModComms.sendRuntimeMessage(ExtraUtils2.instance, mod, key, nbtData);
	}

	@ZenMethod
	public static void sendRuntimeMessageResourceLocation(String mod, String key, String resourceLocation) {
		FMLInterModComms.sendRuntimeMessage(ExtraUtils2.instance, mod, key, new ResourceLocation(resourceLocation));
	}

}
