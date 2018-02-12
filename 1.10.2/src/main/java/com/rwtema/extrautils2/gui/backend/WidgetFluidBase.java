package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.List;

public abstract class WidgetFluidBase extends WidgetBase implements IWidgetServerNetwork, IWidgetCustomJEIIngredient {
	public static final int ux2[] = new int[]{18, 18, 18};
	public static final int uy2[] = new int[]{0, 0, 0};
	public static final int uw2[] = new int[]{7, 7, 7};
	//	public static final int uh2[] = new int[]{64, 64, 64};
	public static final int ux[] = new int[]{32, 0, 50};
	public static final int uy[] = new int[]{0, 0, 0};
	public static final int uw[] = new int[]{18, 18, 18};
	public static final int uh[] = new int[]{33, 18, 65};
	FluidStack curFluid;
	int curCapacity;
	int shape;
	int level;

	public WidgetFluidBase(int x, int y) {
		this(x, y, 0);
	}

	public WidgetFluidBase(int x, int y, int shape) {
		super(x, y, uw[shape], uh[shape]);
		this.curFluid = null;
		this.curCapacity = 0;
		this.shape = shape;
	}


	@Override
	public void addToDescription(XUPacketBuffer packet) {
		packet.writeFluidStack(getCurrentFluid());
		packet.writeInt(getCapacity());
	}

	protected abstract int getCapacity();

	protected abstract FluidStack getCurrentFluid();

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		curFluid = packet.readFluidStack();
		curCapacity = packet.readInt();
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (curFluid != null && curFluid.getFluid() != null && curCapacity > 0) {
			int a = (curFluid.amount * (getH() - 2)) / curCapacity;
			gui.renderFluidTiled(curFluid, manager, guiLeft + getX() + 1, guiTop + getY() - 1 + getH() - a, getW() - 2, a);
		}

		manager.bindTexture(gui.getWidgetTexture());
		gui.drawTexturedModalRect(guiLeft + getX() + getW() - uw2[shape] - 1, guiTop + getY() + 1, ux2[shape] + uw2[shape], uy2[shape], uw2[shape], getH() - 2);
		gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, ux2[shape], uy2[shape], uw2[shape], getH() - 2);
	}

	@Override
	public List<String> getToolTip() {
		if (curFluid == null || curFluid.getFluid() == null)
			return ImmutableList.of(Lang.translateArgs("%s of %s MB", 0, StringHelper.format(curCapacity)));
		return ImmutableList.of(curFluid.getLocalizedName(), Lang.translateArgs("%s of %s MB", StringHelper.format(curFluid.amount), StringHelper.format(curCapacity)));
	}

	@Override
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), ux[shape], uy[shape], uw[shape], uh[shape]);
	}

	@Override
	public Object getJEIIngredient() {
		return curFluid;
	}

	public static class Tank extends WidgetFluidBase {
		final IFluidTank fluidTank;

		public Tank(IFluidTank fluidTank, int x, int y, int shape) {
			super(x, y, shape);
			this.fluidTank = fluidTank;
		}

		@Override
		protected int getCapacity() {
			return fluidTank.getCapacity();
		}

		@Override
		protected FluidStack getCurrentFluid() {
			return fluidTank.getFluid();
		}
	}
}
