package com.rwtema.extrautils2.render;

import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import com.rwtema.extrautils2.utils.datastructures.WeakSet;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LayersHandler {
	public static WeakSet<RenderLivingBase<?>> initRenderers = new WeakSet<>();
	public static WeakLinkedSet<RenderLivingBase<?>> toInit = new WeakLinkedSet<>();

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new LayersHandler());
	}

	@SubscribeEvent
	public void renderStart(RenderLivingEvent.Pre event) {
		if (!initRenderers.contains(event.getRenderer())) {
			initRenderers.add(event.getRenderer());
			toInit.add(event.getRenderer());
		}
	}

	@SubscribeEvent
	public void layers(TickEvent.RenderTickEvent event) {
		if (toInit.isEmpty()) return;
		for (RenderLivingBase<?> renderLivingBase : toInit) {
			addLayers(renderLivingBase);
		}
		toInit.clear();
	}

	private void addLayers(RenderLivingBase<?> renderer) {
		if (renderer instanceof RenderPlayer) {
			RenderPlayer renderPlayer = (RenderPlayer) renderer;
			renderer.addLayer(new LayerWings(renderPlayer));
			renderer.addLayer(new LayerSword(renderPlayer));
		}
	}
}
