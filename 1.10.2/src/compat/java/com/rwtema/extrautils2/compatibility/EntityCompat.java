package com.rwtema.extrautils2.compatibility;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;

public class EntityCompat {

	public static void move(Entity entity, double x, double y, double z) {
		entity.move(x, y, z);
	}

	public static String getKey(EntityLivingBase entity) {
		return EntityList.getEntityString(entity);
	}

	public static String getKey(Class<? extends Entity> entity) {
		return EntityList.field_75626_c.get(entity);
	}
}
