package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.renderer.texture.TextureManager;

import java.util.List;

public abstract class WidgetEnergy extends WidgetBase implements IWidgetServerNetwork {

	public int level;
	public int currentEnergy;
	public int totalEnergy = -1;

	public WidgetEnergy(int x, int y) {
		super(x, y, 18, 54);
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		int y2 = 54 - level;
		gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 160, 0, 18, y2);
		gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY() + y2, 178, y2, 18, level);
	}

	public abstract int getCurrentEnergy();

	public abstract int getTotalEnergy();

	@Override
	public void addToDescription(XUPacketBuffer packet) {
		packet.writeInt(getCurrentEnergy());
		packet.writeInt(getTotalEnergy());
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		currentEnergy = packet.readInt();
		totalEnergy = packet.readInt();
		if (currentEnergy < 0) currentEnergy = 0;
		if (currentEnergy > totalEnergy) currentEnergy = totalEnergy;
		if (totalEnergy < 0) totalEnergy = 0;
		if (totalEnergy == 0) {
			level = 1;
		} else {
			level = 1 + Math.round((currentEnergy * 52F) / totalEnergy);
		}
	}


	@Override
	public List<String> getToolTip() {
		if (totalEnergy == -1) return null;
		return ImmutableList.of(Lang.translateArgs("%s / %s RF", StringHelper.format(currentEnergy), StringHelper.format(totalEnergy)));
	}

}
