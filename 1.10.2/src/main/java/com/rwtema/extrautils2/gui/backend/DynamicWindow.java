package com.rwtema.extrautils2.gui.backend;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

public class DynamicWindow {
	final WindowSide side;
	public int w;
	public int h;
	public int x;
	public int y;
	public float r = 1, g = 1, b = 1;

	public DynamicWindow(WindowSide side) {
		this.side = side;
	}

	public void crop(Set<IWidget> iWidgets) {
		int ax = Integer.MAX_VALUE, ay = Integer.MAX_VALUE, bx = 0, by = 0;
		for (IWidget widget : iWidgets) {
			ax = Math.min(ax, widget.getX());
			ay = Math.min(ay, widget.getY());
			bx = Math.max(bx, widget.getX() + widget.getW());
			by = Math.max(by, widget.getY() + widget.getH());
		}

		w = bx + 4;
		h = by + 4;
	}

	@SideOnly(Side.CLIENT)
	public boolean drawBackgroundOverride(DynamicGui gui, DynamicContainer container) {
		return false;
	}

	public enum WindowSide {
		LEFT, RIGHT
	}
}
