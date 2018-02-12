package com.rwtema.extrautils2.api.lasso;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings({"Convert2Lambda", "unused"})
public class GoldenLassoBlackList {
	public static final HashMap<String, PickupHandler> PICKUP_HANDLERS = Maps.newHashMap();
	public static final LinkedHashMultimap<String, TooltipsHandler> TOOLTIP_HANDLERS = LinkedHashMultimap.create();

	public static void setEntityBlackList(String entityID) {
		PICKUP_HANDLERS.put(entityID, PickupHandler.ALWAYS_BLACKLIST);
	}

	interface PickupHandler {
		PickupHandler ALWAYS_BLACKLIST = new PickupHandler() {
			@Override
			public boolean preventPickup(EntityLivingBase target, EntityPlayer player, ItemStack stack, boolean isMobLasso) {
				return true;
			}
		};

		boolean preventPickup(EntityLivingBase target, EntityPlayer player, ItemStack stack, boolean isMobLasso);
	}

	interface TooltipsHandler {
		@SideOnly(Side.CLIENT)
		void addEntityTooltips(NBTTagCompound savedData, List<String> curTooltips);
	}
}
