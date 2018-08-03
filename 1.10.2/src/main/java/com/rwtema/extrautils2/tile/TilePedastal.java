package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerBase;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;

public class TilePedastal extends XUTile implements ITickable {
	public NBTSerializable.NBTUUID target = registerNBT("target", new NBTSerializable.NBTUUID());


	public SingleStackHandlerBase handler = new SingleStackHandlerBase() {


		@ItemStackNonNull
		@Override
		public ItemStack getStack() {
			if (target.value != null && world instanceof WorldServer) {
				Entity entity = ((WorldServer) world).getEntityFromUuid(target.value);
				if (entity instanceof EntityItem) {
					return ((EntityItem) entity).getItem();
				}
			}
			return StackHelper.empty();
		}

		@Override
		public void setStack(@ItemStackNonNull ItemStack stack) {
			if (!(world instanceof WorldServer)) {
				return;
			}
			if (target.value != null) {
				Entity entity = ((WorldServer) world).getEntityFromUuid(target.value);

				if (entity instanceof EntityItem && !entity.isDead) {
					((EntityItem) entity).setItem(stack);
				}
			}
		}

	};

	@Override
	public void update() {
		if (world.isRemote || target.value == null) {
			return;
		}
		Entity entity = ((WorldServer) world).getEntityFromUuid(target.value);
		if (entity != null) {
			if (entity instanceof EntityItem && !entity.isDead) {
				EntityItem item = (EntityItem) entity;
				item.setDefaultPickupDelay();
				item.setNoGravity(true);
				item.getEntityData().setBoolean("Pedastal", true);
			} else {
				target.value = null;
			}
		}
	}


}
