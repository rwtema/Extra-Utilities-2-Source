package com.rwtema.extrautils2.eventhandlers;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProddingStickHandler {

	public static void register() {
		MinecraftForge.EVENT_BUS.register(new ProddingStickHandler());

	}

	@SubscribeEvent
	public void prod(AttackEntityEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player == null || player.world == null || player.world.isRemote) return;
		ItemStack heldItem = player.getHeldItemMainhand();
		if (StackHelper.isNull(heldItem) || heldItem.getItem() != Items.STICK) return;

		Entity targetEntity = event.getTarget();

		if (!targetEntity.canBeAttackedWithItem() || targetEntity.hitByEntity(player) || !(targetEntity instanceof EntityLivingBase))
			return;


		double i = 1 + EnchantmentHelper.getKnockbackModifier(player) + (player.isSprinting() ? 1 : 0);

		if (i <= 0) return;

		double d0 = targetEntity.motionX;
		double d1 = targetEntity.motionY;
		double d2 = targetEntity.motionZ;

		if (!targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), 0.0001F)) return;

		float rotation = player.rotationYaw * (float) Math.PI / 180.0F;
		targetEntity.addVelocity(
				-MathHelper.sin(rotation) * i * 0.5,
				0.1D,
				MathHelper.cos(rotation) * i * 0.5);
		player.motionX *= 0.6D;
		player.motionZ *= 0.6D;
		player.setSprinting(false);

		if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
			EntityPlayerMP playerMP = (EntityPlayerMP) targetEntity;
			playerMP.connection.sendPacket(new SPacketEntityVelocity(targetEntity));
			SpecialChat.sendChat(playerMP, Lang.chat("Poke!"));
			targetEntity.velocityChanged = false;
			targetEntity.motionX = d0;
			targetEntity.motionY = d1;
			targetEntity.motionZ = d2;
		}

		if (targetEntity instanceof EntityAnimal) {
			((EntityAnimal) targetEntity).setRevengeTarget(null);
		}

		player.setLastAttackedEntity(targetEntity);

		EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);

		heldItem.hitEntity((EntityLivingBase) targetEntity, player);

		if (StackHelper.getStacksize(heldItem) <= 0) {
			player.inventory.deleteStack(heldItem);
		}

		player.addExhaustion(0.3F);

		event.setCanceled(true);
	}
}
