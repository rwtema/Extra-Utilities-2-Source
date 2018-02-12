package com.rwtema.extrautils2.eventhandlers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MobSpawnInAnyLightHandler {

	public static void init() {
		MinecraftForge.EVENT_BUS.register(MobSpawnInAnyLightHandler.class);
	}

	@SubscribeEvent
	public static void check(LivingSpawnEvent.CheckSpawn spawnEvent) {
		EntityLivingBase entityLiving = spawnEvent.getEntityLiving();
		if (entityLiving.world.getDifficulty() == EnumDifficulty.PEACEFUL || !(entityLiving instanceof EntityMob)) {
			return;
		}
		EntityMob mob = (EntityMob) entityLiving;

		IBlockState iblockstate = mob.world.getBlockState((new BlockPos(mob)).down());
		if (!iblockstate.canEntitySpawn(mob)) {
			return;
		}

		if (mob.isNotColliding()) {
			spawnEvent.setResult(Event.Result.ALLOW);
		}
	}
}
