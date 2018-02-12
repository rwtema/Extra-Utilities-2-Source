package com.rwtema.extrautils2.modcompat;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

@ModCompatibility(mod = "baubles")
public class XUCompatBaubles {
	static {
		PlayerHelper.getPlayerInventories.add(player -> {
			IBaublesItemHandler itemHandler = BaublesApi.getBaublesHandler(player);
			if (itemHandler != null) {
				List<ItemStack> stackList = new ArrayList<>(itemHandler.getSlots());
				for (int i = 0; i < itemHandler.getSlots(); i++) {
					stackList.add(itemHandler.getStackInSlot(i));
				}
				return stackList.stream();
			}
			return Stream.of();
		});

		ItemStackHelper.capGetter = () -> BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
		EnumMap<BaubleType, IBauble> simpleBaubles = new EnumMap<>(BaubleType.class);
		ItemStackHelper.getBauble = s -> {
			BaubleType baubleType = BaubleType.valueOf(s);
			return simpleBaubles.computeIfAbsent(baubleType, t -> stack -> t);
		};
	}
}
