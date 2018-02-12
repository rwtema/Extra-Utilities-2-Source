package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileLargishChest extends XUTile implements IDynamicHandler {
	IItemHandler HANDLER = registerNBT("items", new ItemStackHandler(27));

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return HANDLER;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerLargishChest(this, player);
	}


	public static class ContainerLargishChest extends DynamicContainerTile {

		public ContainerLargishChest(TileLargishChest tile, EntityPlayer player) {
			super(tile);
			addTitle(tile);
			crop();
			for (int dy = 0; dy < 3; dy++) {
				for (int dx = 0; dx < 9; dx++) {
					addWidget(new WidgetSlotItemHandler(tile.HANDLER, dy * 9 + dx, 4 + dx * 18, 5 + 9 + 4 + dy * 18));
				}
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
