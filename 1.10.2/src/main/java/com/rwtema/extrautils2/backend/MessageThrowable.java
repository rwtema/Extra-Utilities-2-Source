package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageThrowable {

	public static final MessageThrowable INSTANCE = new Reported();

	private MessageThrowable() {
	}

	public void throwException(String title, String message){
		ExtraUtils2.toThrow =  new RuntimeException(title + " " + message);
	}

	private static class Reported extends MessageThrowable {
		@Override
		@SideOnly(Side.CLIENT)
		public void throwException(String title, String message) {
			ExtraUtils2.toThrow = new XUGuiDisplayError(title, message);
		}
	}

}
