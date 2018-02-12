package com.rwtema.extrautils2.keyhandler;

import com.google.common.base.Throwables;
import java.lang.reflect.Field;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler {
	private static KeyBindingMap hash;

	static {
		for (Field field : KeyBinding.class.getDeclaredFields()) {
			if (field.getType() == KeyBindingMap.class) {
				field.setAccessible(true);
				try {
					hash = (KeyBindingMap) field.get(null);
				} catch (IllegalAccessException e) {
					throw Throwables.propagate(e);
				}
			}
		}
	}

	public static boolean getIsKeyPressed(KeyBinding key) {

		KeyBinding lookup = hash.lookupActive(key.getKeyCode());
		return lookup != null && lookup.isKeyDown();
	}

	public void register() {

	}
}
