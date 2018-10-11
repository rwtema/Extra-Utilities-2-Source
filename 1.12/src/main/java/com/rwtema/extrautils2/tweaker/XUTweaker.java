package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.backend.MessageThrowable;
import com.rwtema.extrautils2.backend.SidedCallable;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.player.IPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "XUTweaker")
@ZenRegister
public class XUTweaker {
	public static final String PACKAGE_NAME_BASE = "extrautilities2.Tweaker.";
	private static Method getIngredient;

	@ZenMethod
	public static void allowSurvivalFlight() {
		GenericAction.run(() -> MinecraftForge.EVENT_BUS.register(new Object() {
			@SubscribeEvent
			public void tick(TickEvent.PlayerTickEvent event) {
				event.player.capabilities.allowFlying = true;
			}
		}), "Enabling flight for all players");
	}

	@ZenMethod
	public static void disableNetherPortals() {
		GenericAction.run(() -> MinecraftForge.EVENT_BUS.register(new Object() {
			@SubscribeEvent
			public void tick(BlockEvent.PortalSpawnEvent event) {
				event.setCanceled(true);
			}
		}), "Disabling nether portal creation");
	}

	@ZenMethod
	public static boolean isPlayerFake(IPlayer player) {
		Object internal = player.getInternal();
		return !(internal instanceof EntityPlayer && PlayerHelper.isPlayerReal((EntityPlayer) internal));
	}

	@ZenMethod
	public static boolean openBookScreen(IPlayer player, String[] pageData) {
		if (!player.getWorld().isRemote()) return false;

		Object internal = player.getInternal();
		if (!(internal instanceof EntityPlayer)) return false;

		return new SidedCallable<Boolean>() {
			@Override
			@SideOnly(Side.CLIENT)
			public Boolean callClient() {
				ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
				NBTTagList pages = new NBTTagList();
				for (String s : pageData) {
					pages.appendTag(new NBTTagString(s));
				}
				book.setTagInfo("pages", pages);

				Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBook((EntityPlayer) internal, book, false));
				return true;
			}

			@Override
			@SideOnly(Side.SERVER)
			public Boolean callServer() {
				return false;
			}
		}.callUnchecked();
	}

	@ZenMethod
	public static void throwFatalException(String message) {
		CraftTweakerAPI.logError(message);
		GenericAction.run(() -> MessageThrowable.INSTANCE.throwException("Problem", message), "Scheduling Error Screen [" + message + "]");
	}

	public static ItemStack createItemStack(IItemStack stack) {
		Object internal = stack.getInternal();
		return internal instanceof ItemStack ? ((ItemStack) internal).copy() : ItemStack.EMPTY;
	}

	public static FluidStack createFluidStack(ILiquidStack stack) {
		if (stack == null) return null;
		Object internal = stack.getInternal();
		return internal instanceof FluidStack ? ((FluidStack) internal) : null;
	}

	public static IItemStack getIItemStack(ItemStack input){
		return (IItemStack) getIIngredient(input);
	}

	public static ILiquidStack getILiquidStack(FluidStack input){
		return (ILiquidStack) getIIngredient(input);
	}

	public static IIngredient getIIngredient(Object input) {
		if (getIngredient == null) {
			try {
				getIngredient = ReflectionHelper.findMethod(Class.forName("crafttweaker.api.minecraft.CraftTweakerMC"), "getIIngredient", null, Object.class);
			} catch (ClassNotFoundException | ReflectionHelper.UnableToFindMethodException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			return (IIngredient) getIngredient.invoke(null, input);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
