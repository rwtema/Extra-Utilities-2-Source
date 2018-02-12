package com.rwtema.extrautils2.network;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NetworkHandler.XUPacket
public class SpecialChat extends XUPacketServerToClient {
	final static int BASE_LINE_ID = -357962104;
	ITextComponent chat;
	int id;

	public SpecialChat() {

	}

	public SpecialChat(ITextComponent chat, int id) {
		this.chat = chat;
		this.id = id;
	}

	public static void sendChat(EntityPlayer player, final ITextComponent chat) {
		sendChat(player, chat, BASE_LINE_ID);
	}

	public static void sendChat(EntityPlayer player, final ITextComponent chat, final int id) {
		if (player.world.isRemote) {
			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					GuiNewChat chatGUI = Minecraft.getMinecraft().ingameGUI.getChatGUI();
					if (chat == null) {
						chatGUI.deleteChatLine(id);
					} else {
						chatGUI.printChatMessageWithOptionalDeletion(chat, id);
					}
				}
			});
		} else {
			NetworkHandler.sendPacketToPlayer(new SpecialChat(chat, id), player);
		}
	}

	@Override
	public void writeData() throws Exception {
		writeInt(id);
		if (chat != null) {
			writeBoolean(false);
			writeChatComponent(chat);
		} else {
			writeBoolean(true);
		}

	}

	@Override
	public void readData(EntityPlayer player) {
		id = readInt();
		if (readBoolean()) {
			chat = null;
		} else {
			chat = readChatComponent();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Runnable doStuffClient() {
		return new Runnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				GuiNewChat chatGUI = Minecraft.getMinecraft().ingameGUI.getChatGUI();
				if (chat == null) {
					chatGUI.deleteChatLine(id);
				} else {
					chatGUI.printChatMessageWithOptionalDeletion(chat, id);
				}
			}
		};
	}
}
