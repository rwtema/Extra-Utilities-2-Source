package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class TileMinChest extends XUTile implements IDynamicHandler {
	IItemHandler HANDLER = registerNBT("item", new SingleStackHandler(){
		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	});

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMiniChest(this, player);
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return HANDLER;
	}

	public static class ContainerMiniChest extends DynamicContainerTile {

		public ContainerMiniChest(TileMinChest tile, EntityPlayer player) {
			super(tile);
			addTitle(XU2Entries.miniChest.newStack().getDisplayName());
			crop();
			addWidget(new WidgetSlotItemHandler(tile.HANDLER, 0, DynamicContainer.centerSlotX, height + 4));
			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
