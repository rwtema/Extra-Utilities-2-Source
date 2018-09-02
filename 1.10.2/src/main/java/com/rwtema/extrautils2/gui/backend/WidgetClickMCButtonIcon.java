package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

public abstract class WidgetClickMCButtonIcon extends WidgetClickMCButtonBase {


	public WidgetClickMCButtonIcon(int x, int y) {
		super(x, y, 18, 18);
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		gui.renderStack(getStack() , guiLeft + getX() + 1, guiTop + getY() + 1, "");
	}

	protected abstract ItemStack getStack();
}
