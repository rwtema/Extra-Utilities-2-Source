package com.rwtema.extrautils2.gui.backend;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IDynamicHandler {
	DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z);
}
