package com.rwtema.extrautils2.banner;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

public class Banner {
	public static void init() {
		Class<?>[] paramClasses = {String.class, String.class, ItemStack.class};
		EnumHelper.addEnum(CompatHelper.getBannerEnumClass(), "ANGEL", paramClasses, "angel", "ang", XU2Entries.angelBlock.newStack());
		EnumHelper.addEnum(CompatHelper.getBannerEnumClass(), "XU_LAW", paramClasses, "xu_law", "xulaw", XU2Entries.lawSword.newStack());
	}
}
