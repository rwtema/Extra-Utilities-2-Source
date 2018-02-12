package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

public class WidgetClickMCButtonChoiceEnum<T extends Enum<T>> extends WidgetClickMCButtonChoices<T> {
	final NBTSerializable.NBTEnum<T> nbtEnum;

	public WidgetClickMCButtonChoiceEnum(int x, int y, NBTSerializable.NBTEnum<T> nbtEnum) {
		this(x, y, nbtEnum, anEnum -> StringHelper.capitalizeProp(anEnum.name()), t -> null, t -> null);
	}

	public WidgetClickMCButtonChoiceEnum(int x, int y, NBTSerializable.NBTEnum<T> nbtEnum, Function<T, String> caption, Function<T, String> tooltip, Function<T, ItemStack> stack) {
		super(x, y);
		this.nbtEnum = nbtEnum;
		Class<T> aClass = nbtEnum.value.getDeclaringClass();
		for (T anEnum : aClass.getEnumConstants()) {
			ItemStack itemStack = stack.apply(anEnum);
			String name = caption.apply(anEnum);
			if (itemStack == null && name == null) {
				throw new IllegalStateException();
			} else if (itemStack == null) {
				addChoice(anEnum, name, tooltip.apply(anEnum));
			} else if (name != null) {
				addChoice(anEnum, name, itemStack, tooltip.apply(anEnum));
			} else {
				addChoice(anEnum, itemStack, tooltip.apply(anEnum));
			}
		}
	}

	public static <T extends Enum<T>> int getWidth(Class<T> clazz) {
		int w = 0;
		for (T t : clazz.getEnumConstants()) {
			w = Math.max(w, 8 + ExtraUtils2.proxy.apply(DynamicContainer.STRING_WIDTH_FUNCTION,
					StringHelper.capitalizeProp(t.name())));
		}
		return w;
	}

	@Override
	protected void onSelectedServer(T marker) {
		nbtEnum.value = marker;
	}

	@Override
	public T getSelectedValue() {
		return nbtEnum.value;
	}
}
