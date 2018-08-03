package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.entity.EntityBoomerang;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.util.ResourceLocation;

public class EntityCompat {
	public static void move(EntityBoomerang entityBoomerang, double motionX, double motionY, double motionZ) {
		entityBoomerang.move(MoverType.SELF, motionX, motionY, motionZ);
	}

	public static ResourceLocation getKey(EntityLivingBase entity) {
		return EntityList.getKey(entity);
	}

	public static String getKey(Class<? extends Entity> clazz) {
		return EntityList.getKey(clazz).toString();
	}
}
