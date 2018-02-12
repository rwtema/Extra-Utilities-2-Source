package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import org.lwjgl.opengl.GL11;

public class WidgetSlotDisablable extends WidgetSlot implements IWidgetServerNetwork {
	boolean enabled = true;
	String methodName;

	public WidgetSlotDisablable(IInventory inv, int slot, int x, int y, String methodName) {
		super(inv, slot, x, y);
		this.methodName = methodName;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
		return enabled;
	}

	public boolean isEnabled() {
		try {
			return (Boolean) this.inventory.getClass().getMethod(methodName).invoke(inventory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (!enabled) {
			boolean blendLevel = GlStateManager.blendState.blend.currentState;

			if (!blendLevel) {
				GlStateManager.enableBlend();
			}

			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1, 1, 1, 0.4F);
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
			GlStateManager.color(1, 1, 1, 1);

			if (!blendLevel) {
				GlStateManager.disableBlend();
			}
		}
	}

	@Override
	public void addToDescription(XUPacketBuffer packet) {
		packet.writeBoolean(enabled);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		enabled = packet.readBoolean();
	}
}
