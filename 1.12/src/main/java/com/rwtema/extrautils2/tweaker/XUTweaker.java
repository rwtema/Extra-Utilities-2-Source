package com.rwtema.extrautils2.tweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "XUTweaker")
@ZenRegister
public class XUTweaker {
	public static final String PACKAGE_NAME_BASE = "extrautilities2.Tweaker.";

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
		}), "Enabling flight for all players");
	}

	public static ItemStack createItemStack(IItemStack stack) {
		Object internal = stack.getInternal();
		return internal instanceof ItemStack ? (ItemStack) internal : ItemStack.EMPTY;
	}

	public static FluidStack createFluidStack(ILiquidStack stack) {
		Object internal = stack.getInternal();
		return internal instanceof FluidStack ? ((FluidStack) internal) : null;
	}
}
