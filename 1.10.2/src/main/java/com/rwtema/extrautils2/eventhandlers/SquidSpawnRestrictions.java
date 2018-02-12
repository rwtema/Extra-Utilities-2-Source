package com.rwtema.extrautils2.eventhandlers;

import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SquidSpawnRestrictions {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void denySquidSpawn(LivingSpawnEvent.CheckSpawn event){
		if(event.getResult() != Event.Result.DEFAULT) return;

		if (event.getEntity() instanceof EntitySquid) {
			World world = event.getWorld();
			BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
			for (BlockPos.MutableBlockPos blockPos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
				TileEntity entity = world.getTileEntity(blockPos);
				if(entity != null){
					event.setResult(Event.Result.DENY);
				}
			}
		}

	}
}
