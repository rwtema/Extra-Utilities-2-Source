package com.rwtema.extrautils2.backend.model;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.utils.datastructures.WeakSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class CachedRenderers {
	private final static WeakSet<IClientClearCache> cachedRenderers = new WeakSet<>();

	static {
		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
					@Override
					public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
						synchronized (cachedRenderers) {
							for (IClientClearCache cachedRenderer : cachedRenderers) {
								cachedRenderer.clientClear();
							}
						}
					}
				});
			}
		});
	}

	public static void register(IClientClearCache clientClearCache) {
		synchronized (cachedRenderers) {
			cachedRenderers.add(clientClearCache);
		}
	}

}
