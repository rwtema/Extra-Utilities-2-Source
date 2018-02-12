package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public abstract class WidgetTextDataScroll extends WidgetTextData implements IAdditionalWidgets {
	WidgetScrollBar scrollBar;
	List<String> strings = new ArrayList<>();
	int numLines;

	public WidgetTextDataScroll(int x, int y, int w, int h) {
		super(x, y, w - WidgetScrollBar.BAR_WIDTH, h);
		scrollBar = new WidgetScrollBar(x + w - WidgetScrollBar.BAR_WIDTH, y, h, 0, 1) {
			@Override
			protected void onChange() {

			}
		};
		scrollBar.hideWhenInvalid = true;
		numLines = h / 9;
	}

	@Override
	public List<IWidget> getAdditionalWidgets() {
		return Lists.newArrayList(scrollBar);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		if (msg != null)
			strings = this.gui.getFontRenderer().listFormattedStringToWidth(msg, w);
		else
			strings = new ArrayList<>();
		scrollBar.setValues(0, Math.max(0, strings.size() - numLines));
	}

	@Override
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
//		super.renderBackground(manager, gui, guiLeft, guiTop);
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (msg == null || strings.isEmpty()) return;
		GlStateManager.color(1, 1, 1, 1);

		int scrollValue = scrollBar.scrollValue;
		final List<String> strings = this.strings;
		for (int i = scrollValue; i < Math.min(strings.size(), scrollValue + numLines); i++) {
			gui.getFontRenderer().drawString(strings.get(i), guiLeft + getX(), guiTop + getY() + (i - scrollValue) * 9, 4210752);
		}
	}
}
