package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetButton extends WidgetBase implements IWidgetClientNetwork {
	public String text;
	@SideOnly(Side.CLIENT)
	GuiButton button;
	private DynamicContainer container;


	public WidgetButton(int x, int y, int w, int h, String text) {
		super(x, y, w, h);
		this.text = text;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addToGui(DynamicGui gui) {
		super.addToGui(gui);
		DynamicWindow window = gui.container.getWindowOwner().get(this);
		int x = this.x + gui.guiLeft + (window != null ? window.x : 0);
		int y = this.y + gui.guiTop + (window != null ? window.y : 0);
		button = createButton(x, y, gui.nextButtonID());
		gui.addButton(this, button);
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public GuiButton createButton(int x, int y, int id) {

		return new GuiButton(id, x, y, w, h, text);
	}

	@SideOnly(Side.CLIENT)
	public void onClientClick() {
		sendClickToServer();
	}

	public void onClickServer(XUPacketBuffer buffer){

	}

	@Override
	public void receiveClientPacket(XUPacketBuffer buffer) {
		onClickServer(buffer);
	}

	@Override
	public void addToContainer(DynamicContainer container) {
		this.container = container;
	}

	@SideOnly(Side.CLIENT)
	public void sendClickToServer(){
		sendClickToServer(null);
	}

	@SideOnly(Side.CLIENT)
	public void sendClickToServer(XUPacketBuffer buffer) {
		container.sendInputPacket(this, buffer);
	}


}