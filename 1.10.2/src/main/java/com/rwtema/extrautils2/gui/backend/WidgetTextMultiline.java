package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

public class WidgetTextMultiline extends WidgetText {



	public WidgetTextMultiline(int x, int y, String msg) {
		super(x, y, msg);
	}

	public WidgetTextMultiline(int x, int y, String msg, int w) {
		this(x, y, w, ExtraUtils2.proxy.apply(DynamicContainer.STRING_HEIGHTS, Pair.of(msg, w)), 1, 4210752, msg);
	}

	public WidgetTextMultiline(int x, int y, int align, int color, String msg) {
		super(x, y, align, color, msg);
	}

	public WidgetTextMultiline(int x, int y, int w, int h, int align, int color, String msg) {
		super(x, y, w, h, align, color, msg);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (msg == null) return;

		int x = getX() + ((1 - align) * (getW() - gui.getFontRenderer().getStringWidth(getMsgClient()))) / 2;
		gui.getFontRenderer().drawSplitString(msg, guiLeft + x, guiTop + getY(), getW(), 4210752);
		manager.bindTexture(gui.getWidgetTexture());
	}
}
