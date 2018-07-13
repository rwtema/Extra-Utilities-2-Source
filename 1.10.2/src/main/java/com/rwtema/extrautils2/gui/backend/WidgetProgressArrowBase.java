package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class WidgetProgressArrowBase extends WidgetBase {
	public final static int ARROW_WIDTH = 22;
	public final static int ARROW_HEIGHT = 17;
	public static Consumer<String> setCategory = null;
	byte curWidth;

	public WidgetProgressArrowBase(int x, int y) {
		this(x, y, (byte) 0);
	}

	public WidgetProgressArrowBase(int x, int y, byte curWidth) {
		super(x, y, ARROW_WIDTH, ARROW_HEIGHT);
		this.curWidth = curWidth;
	}

	@Nonnull
	public static IWidget getJEIWidget(final Runnable runnable, final WidgetProgressArrowBase base) {
		if (runnable == null) return IWidget.NULL_INSTANCE;
		return new IWidgetMouseInput() {
			@Override
			public void mouseClicked(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
				if (mouseOver && mouseButton == 0) {
					runnable.run();
				}
			}

			@Override
			public void mouseReleased(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {

			}

			@Override
			public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastMove, boolean mouseOver) {

			}

			@Override
			public void mouseWheelScroll(int delta, boolean mouseOver) {

			}

			@Override
			public void mouseTick(int mouseX, int mouseY, boolean mouseOver) {

			}

			@Override
			public boolean usesMouseWheel() {
				return false;
			}

			@Override
			public int getX() {
				return base.getX();
			}

			@Override
			public int getY() {
				return base.getY();
			}

			@Override
			public int getW() {
				return base.getW();
			}

			@Override
			public int getH() {
				return base.getH();
			}

			@Override
			public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

			}

			@Override
			public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

			}

			@Override
			public void addToContainer(DynamicContainer container) {

			}

			@Override
			public List<String> getToolTip() {
				return ImmutableList.of(Lang.translate(ChatFormatting.BLUE + "Click for recipes" + ChatFormatting.RESET));
			}

			@Override
			public void addToGui(DynamicGui gui) {

			}
		};
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (curWidth > 0 && curWidth != -1) {
			manager.bindTexture(gui.getWidgetTexture());
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 98, ARROW_HEIGHT - 1, curWidth, ARROW_HEIGHT - 1);
		}
	}

	@Override
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (curWidth == -1) {
			manager.bindTexture(gui.getWidgetTexture());
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 98, (ARROW_HEIGHT - 1) * 2, ARROW_WIDTH, ARROW_HEIGHT - 1);
		} else if (curWidth < ARROW_WIDTH) {
			manager.bindTexture(gui.getWidgetTexture());
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 98, 0, ARROW_WIDTH, ARROW_HEIGHT - 1);
		}
	}

	public List<String> getErrorMessage() {
		return null;
	}

	public IWidget getJEIWidget(String uid) {
		if (setCategory == null) return IWidget.NULL_INSTANCE;

		Runnable runnable = () -> setCategory.accept(uid);

		final WidgetProgressArrowBase base = this;
		return getJEIWidget(runnable, base);
	}
}
