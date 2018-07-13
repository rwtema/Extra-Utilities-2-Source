package com.rwtema.extrautils2.keyhandler;

import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.packets.PacketUseItemAlt;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class KeyAlt {
	private static Map<EntityPlayer, Boolean> keyMap = Collections.synchronizedMap(new WeakHashMap<EntityPlayer, Boolean>());

	static {
		MinecraftForge.EVENT_BUS.register(new KeyAlt());
		LogHelper.oneTimeInfo("Key Alt Register");
	}

	public static boolean isAltSneaking(EntityPlayer player) {
		return KeyAlt.keyMap.get(player) == Boolean.TRUE;
	}

	public static void setValue(EntityPlayer player, boolean sprint) {
		KeyAlt.keyMap.put(player, sprint);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPress(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		Boolean isKeyPressed = KeyHandler.getIsKeyPressed(mc.gameSettings.keyBindSprint);
		if (keyMap.get(mc.player) != isKeyPressed) {
			keyMap.put(mc.player, isKeyPressed);
			NetworkHandler.sendPacketToServer(new PacketUseItemAlt(isKeyPressed));
		}
	}
}
