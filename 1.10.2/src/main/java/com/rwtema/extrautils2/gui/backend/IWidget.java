package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface IWidget {
	public static final IWidget NULL_INSTANCE = new IWidget() {

		@Override
		public int getX() {
			return 0;
		}

		@Override
		public int getY() {
			return 0;
		}

		@Override
		public int getW() {
			return 0;
		}

		@Override
		public int getH() {
			return 0;
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
			return null;
		}

		@Override
		public void addToGui(DynamicGui gui) {

		}
	};

	int getX();

	int getY();

	int getW();

	int getH();

	@SideOnly(Side.CLIENT)
	default void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

	}

	@SideOnly(Side.CLIENT)
	default void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

	}

	default void addToContainer(DynamicContainer container) {

	}

	List<String> getToolTip();

	@SideOnly(Side.CLIENT)
	default void addToGui(DynamicGui gui) {

	}

	default void onContainerClosed(DynamicContainer container, EntityPlayer playerIn) {
	}
}
