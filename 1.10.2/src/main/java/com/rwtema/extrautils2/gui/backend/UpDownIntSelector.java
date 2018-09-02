package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;

public abstract class UpDownIntSelector implements Iterable<WidgetBase> {
	final WidgetClickMCButtonBase up, down;
	final WidgetTextData text;

	public static int getOffset(){
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			return 10;
		return 1;
	}

	protected UpDownIntSelector(int x, int y, int w) {
		up = new WidgetClickMCButtonIcon(x + w / 2 - 9, y) {
			@Override
			protected ItemStack getStack() {
				return ItemIngredients.Type.BUTTON_UP.newStack();
			}

			@Nullable
			@Override
			public XUPacketBuffer getPacketToSend(int mouseButton) {
				if(mouseButton == 0){
					XUPacketBuffer buffer = new XUPacketBuffer();
					buffer.writeInt(getValue() + getOffset());
					return buffer;
				}
				return null;
			}

			@Override
			public void receiveClientPacket(XUPacketBuffer buffer) {
				setValue(buffer.readInt());
			}
		};
		text = new WidgetTextData(x, y + 18+2, w, 9, 0, 0x404040) {
			@Override
			public void addToDescription(XUPacketBuffer packet) {
				packet.writeInt(getValue());
			}

			@Override
			protected String constructText(XUPacketBuffer packet) {
				int newVal = packet.readInt();
				setValue(newVal);
				return StringHelper.format(newVal);
			}
		};

		down = new WidgetClickMCButtonIcon(x  + w / 2 - 9, y + 4 + 18 + 9) {
			@Override
			protected ItemStack getStack() {
				return ItemIngredients.Type.BUTTON_DOWN.newStack();
			}

			@Nullable
			@Override
			public XUPacketBuffer getPacketToSend(int mouseButton) {
				if (mouseButton == 0) {
					XUPacketBuffer buffer = new XUPacketBuffer();
					buffer.writeInt(getValue() - getOffset());
					return buffer;
				}
				return null;
			}

			@Override
			public void receiveClientPacket(XUPacketBuffer buffer) {
				setValue(buffer.readInt());
			}
		};
	}

	public abstract int getValue();

	public abstract void setValue(int val);

	@Nonnull
	@Override
	public Iterator<WidgetBase> iterator() {
		return Stream.of(up, down, text).iterator();
	}
}
