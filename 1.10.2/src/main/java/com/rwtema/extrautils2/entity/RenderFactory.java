package com.rwtema.extrautils2.entity;

import com.google.common.base.Throwables;
import java.lang.reflect.Constructor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class RenderFactory<T extends Entity> implements IRenderFactory<T> {
	Class<Render<? super T>> clazz;

	@SuppressWarnings("unchecked")
	public RenderFactory(Class clazz) {
		this.clazz = clazz;
	}

	public static void registerRenderer(Class<EntityBoomerang> entityClass, Class<RenderEntityBoomerang> renderEntityClass) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, new RenderFactory<Entity>(renderEntityClass));
	}

	@Override
	public Render<? super T> createRenderFor(RenderManager manager) {
		try {
			Constructor<? extends Render<? super T>> constructor = clazz.getConstructor(RenderManager.class);
			return constructor.newInstance(manager);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}

	}


}
