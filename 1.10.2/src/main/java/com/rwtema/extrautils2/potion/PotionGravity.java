package com.rwtema.extrautils2.potion;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class PotionGravity extends XUPotion {

	public PotionGravity() {
		super("Gravity", true, 0x202020);
	}

	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		return true;
	}

	@Override
	public void performEffect(@Nonnull EntityLivingBase entity, int p_76394_2_) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			if (player.capabilities.isFlying) {
				player.capabilities.isFlying = false;
				player.sendPlayerAbilities();
			}

			if (entity.motionY > 0) {
				entity.motionY *= 1;
			}
		}

		double dist = 5;
		Vec3d vec3d = entity.getPositionVector();
		Vec3d vec3d2 = vec3d.addVector(0, -dist, 0);
		RayTraceResult result = entity.world.rayTraceBlocks(vec3d, vec3d2, false, false, false);
		if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
			Vec3d hitVec = result.hitVec;
			dist = hitVec.distanceTo(vec3d);
		}

		if (!entity.onGround) {
			entity.motionY -= 0.04 * dist;
		}
	}
}
