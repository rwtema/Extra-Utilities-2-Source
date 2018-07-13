package com.rwtema.extrautils2.eventhandlers;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.XUProxy;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.XURandom;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CreativeEPHandler {

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new CreativeEPHandler());
	}

	@SubscribeEvent
	public void enderRightClick(PlayerInteractEvent.RightClickItem event) {

		final EntityPlayer player = event.getEntityPlayer();
		if (!player.capabilities.isCreativeMode) return;
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (StackHelper.isNull(currentItem) || currentItem.getItem() != Items.ENDER_PEARL) return;

		World worldIn = event.getWorld();
		event.setCanceled(true);


		worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (XURandom.rand.nextFloat() * 0.4F + 0.8F));

		if (!worldIn.isRemote) {
			worldIn.spawnEntity(new EntityEnderPearl(worldIn, player));
		} else {
			ExtraUtils2.proxy.run(XUProxy.sendRightClick);
		}

		player.addStat(StatList.getObjectUseStats(Items.ENDER_PEARL));

		StackHelper.decrease(currentItem);

		if (StackHelper.isEmpty(currentItem)) {
			player.inventory.setInventorySlotContents(player.inventory.currentItem, StackHelper.empty());
			ForgeEventFactory.onPlayerDestroyItem(player, currentItem, EnumHand.MAIN_HAND);
		}

		if (player instanceof EntityPlayerMP) {
			((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
		}

	}
}
