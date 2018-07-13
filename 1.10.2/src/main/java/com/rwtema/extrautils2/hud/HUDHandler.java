package com.rwtema.extrautils2.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class HUDHandler {
	static ArrayList<IHudHandler> handlers = new ArrayList<>();

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new HUDHandler());
	}

	public static void register(IHudHandler handler) {
		handlers.add(handler);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void hudDraw(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
		GuiIngameForge currentScreen = (GuiIngameForge) Minecraft.getMinecraft().ingameGUI;
		for (IHudHandler handler : handlers) {
			handler.render(currentScreen, event.getResolution(), event.getPartialTicks());
		}
	}


}
