package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetFluidIndicator extends WidgetBase implements IWidgetServerNetwork {
	private final int u;
	private final int v;
	@SideOnly(Side.CLIENT)
	private FluidStack renderStack;
	@SideOnly(Side.CLIENT)
	private float renderPercent;

	public WidgetFluidIndicator(int x, int y) {
		this(x, y, 18, 18, 0, 0);
	}

	public WidgetFluidIndicator(int x, int y, int w, int h, int u, int v) {
		super(x, y, w, h);
		this.u = u;
		this.v = v;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		int h2 = getH() - 2;
		int h = Math.round(MathHelper.clamp(renderPercent, 0, 1) * h2);
		if (renderStack == null || h == 0)
			return;

		Fluid fluid = renderStack.getFluid();
		if (fluid == null) return;
		ResourceLocation still = fluid.getStill(renderStack);
		manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still.toString());

		gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1 + h2 - h,
				getW() - 2,
				h,
				sprite.getMinU(),
				sprite.getInterpolatedV(16 - (h * 16.0F) / h2),
				sprite.getMaxU(),
				sprite.getMaxV());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), u, v, getW(), getH());
	}

	public abstract FluidStack getFluid();

	public abstract float getFillPercent();

	@Override
	public void addToDescription(XUPacketBuffer packet) {
		packet.writeFluidStack(getFluid());
		packet.writeFloat(getFillPercent());
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		renderStack = packet.readFluidStack();
		renderPercent = packet.readFloat();
	}
}
