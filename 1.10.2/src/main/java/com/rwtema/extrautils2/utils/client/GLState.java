package com.rwtema.extrautils2.utils.client;

import java.util.LinkedList;
import net.minecraft.client.renderer.GlStateManager;

public abstract class GLState<T> {

	transient final static LinkedList<GLState<?>> toReset = new LinkedList<>();

	private final T value;
	private T oldValue;

	public GLState(T value) {
		this.value = value;
	}

	public static void resetStateQuads() {
		if (!toReset.isEmpty()) {
			synchronized (toReset) {
				GLState<?> poll;
				while ((poll = toReset.poll()) != null) {
					poll.reset();
				}
			}
		}
	}

	public void setValue() {
		oldValue = getCachedState();
		if (!value.equals(oldValue)) {
			synchronized (toReset) {
				toReset.addFirst(this);
			}
		}
		setGLState(value);
	}

	public void reset() {
		setGLState(oldValue);
	}

	public abstract T getCachedState();

	public abstract void setGLState(T value);

	public static class DepthState extends GLState<Boolean> {

		public DepthState(boolean value) {
			super(value);
		}

		@Override
		public Boolean getCachedState() {
			return GlStateManager.depthState.maskEnabled;
		}

		@Override
		public void setGLState(Boolean value) {
			GlStateManager.depthMask(value);
		}

	}
}

