package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;

public abstract class WidgetClickMCButtonBoolean extends WidgetClickMCButtonChoices<Boolean> {
	public WidgetClickMCButtonBoolean(int x, int y, String text, String tooltip) {
		super(x, y);
		addChoice(Boolean.TRUE, text, ItemIngredients.Type.SYMBOL_TICK.newStack(), tooltip);
		addChoice(Boolean.FALSE, text, ItemIngredients.Type.SYMBOL_CROSS.newStack(), tooltip);
	}



	public static class NBTBoolean extends WidgetClickMCButtonBoolean {
		final NBTSerializable.NBTBoolean nbtBoolean;

		public NBTBoolean(int x, int y, NBTSerializable.NBTBoolean nbtBoolean, String text, String tooltip) {
			super(x, y, text, tooltip);
			this.nbtBoolean = nbtBoolean;
		}

		@Override
		protected void onSelectedServer(Boolean marker) {
			nbtBoolean.value = marker;
		}

		@Override
		public Boolean getSelectedValue() {
			return nbtBoolean.value;
		}
	}
}
