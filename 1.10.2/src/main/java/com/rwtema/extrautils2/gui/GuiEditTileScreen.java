package com.rwtema.extrautils2.gui;

import com.rwtema.extrautils2.blocks.BlockScreen;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.tile.TileScreen;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiEditTileScreen extends GuiScreen {
	private final TileScreen screen;
	String text;
	int timer = 0;
	private GuiButton doneBtn;

	public GuiEditTileScreen(TileScreen screen) {
		this.screen = screen;
		text = screen.id;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		timer++;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, I18n.format("gui.done")));
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);

	}

	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {

				NetworkHandler.sendPacketToServer(new TileScreen.PacketEditScreen(screen.getPos(), text));
				this.mc.displayGuiScreen(null);
			}
		}
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			this.mc.displayGuiScreen(null);
		} else if (keyCode == 28) {
			this.actionPerformed(this.doneBtn);
		}

		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			String string = GuiScreen.getClipboardString();
			string = string.replace("http:", "");
			string = string.replace("https:", "");
			string = string.replace("www.", "");
			string = string.replace("i.imgur.com", "");
			string = string.replace("imgur.com", "");
			string = string.replace("gallery/", "");
			string = string.replace(".png", "");
			string = string.replace("/a/", "");
			string = string.replace("/", "");
			int i = string.lastIndexOf('.');
			if (i != -1) {
				string = string.substring(0, i);
			}

			string = string.replace(".", "");
			if (string.length() > 10 || TileScreen.illegalPatternControlCode.matcher(string).find()) {
				return;
			}

			text = string;
			return;

		}

		String s = text;

		if (keyCode == 14 && s.length() > 0) {
			s = s.substring(0, s.length() - 1);
		}

		if (!TileScreen.illegalPatternControlCode.matcher("" + typedChar).find() && ChatAllowedCharacters.isAllowedCharacter(typedChar) && s.length() <= 12) {
			s = s + typedChar;
		}

		text = s;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawCenteredString(this.fontRenderer, Lang.translate("Select Screen Display Image"), this.width / 2, 40, 16777215);
		this.drawCenteredString(this.fontRenderer, "i.imgur.com/" + TextFormatting.BOLD + text + TextFormatting.RESET + ".png", this.width / 2, 80, 16777215);
		if (BlockScreen.maxSize > 0)
			this.drawCenteredString(this.fontRenderer, Lang.translateArgs("Max file size is %s kb", BlockScreen.maxSize >> 10), this.width / 2, 120, 16777215);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
