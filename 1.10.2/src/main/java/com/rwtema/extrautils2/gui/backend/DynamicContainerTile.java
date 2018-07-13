package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.tile.XUTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class DynamicContainerTile extends DynamicContainer {
	protected final TileEntity tile;

	public DynamicContainerTile(TileEntity tile) {
		this(tile, 0, 0);
	}

	public DynamicContainerTile(TileEntity tile, int y, int size) {
		this.tile = tile;
		if (size != 0) {
			addWidget(new WidgetTileBackground(tile.getWorld(), tile.getPos(), y, size));
		}
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		return XUTile.isLoaded(tile);
	}


	@Override
	public void onSlotChanged(int index) {
		super.onSlotChanged(index);
		tile.markDirty();
	}
}
