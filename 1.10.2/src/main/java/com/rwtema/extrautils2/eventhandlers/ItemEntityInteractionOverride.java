package com.rwtema.extrautils2.eventhandlers;

import com.google.common.collect.Sets;
import java.util.HashSet;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemEntityInteractionOverride {
	public static final HashSet<Item> items = Sets.newHashSet();

	static {
		MinecraftForge.EVENT_BUS.register(new ItemEntityInteractionOverride());
	}

	@SubscribeEvent
	public void allowItemToInteract(PlayerInteractEvent.EntityInteract event) {
		EntityPlayer player = event.getEntityPlayer();
		EnumHand hand = event.getHand();
		ItemStack itemstack = player.getHeldItem(hand);
		if (StackHelper.isNonNull(itemstack) && items.contains(itemstack.getItem())) {
			Entity entity = event.getTarget();

			if (entity instanceof EntityLivingBase) {
				if (itemstack.interactWithEntity(player, (EntityLivingBase) entity, hand)) {
					if (StackHelper.getStacksize(itemstack) <= 0) {
						player.inventory.deleteStack(itemstack);
					}

					event.setCanceled(true);
				}
			}
		}
	}

}
