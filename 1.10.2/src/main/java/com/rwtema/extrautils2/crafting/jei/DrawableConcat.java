package com.rwtema.extrautils2.crafting.jei;

import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DrawableConcat implements IDrawable {
	final List<Entry> entryList;

	public DrawableConcat() {
		this(new ArrayList<>());
	}

	public DrawableConcat(List<Entry> entryList) {
		this.entryList = entryList;
	}

	public DrawableConcat add(IDrawable drawable, int x_offset, int y_offset) {
		entryList.add(new Entry(drawable, x_offset, y_offset));
		return this;
	}

	@Override
	public int getWidth() {
		int w = 0;
		for (Entry entry : entryList) {
			w = Math.max(w, entry.offset_x + entry.drawable.getWidth());
		}
		return w;
	}

	@Override
	public int getHeight() {
		int h = 0;
		for (Entry entry : entryList) {
			h = Math.max(h, entry.offset_y + entry.drawable.getHeight());
		}
		return h;
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft) {
		draw(minecraft, 0, 0);
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset) {
		for (Entry entry : entryList) {
			entry.drawable.draw(minecraft, entry.offset_x + xOffset, entry.offset_y + yOffset);
		}
	}

	public static class Entry {
		final IDrawable drawable;
		final int offset_x;
		final int offset_y;

		public Entry(IDrawable drawable, int offset_x, int offset_y) {
			this.drawable = drawable;
			this.offset_x = offset_x;
			this.offset_y = offset_y;
		}
	}
}
