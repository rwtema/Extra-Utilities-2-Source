package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.tile.XUTile;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class XUTileItemStackHandler extends ItemStackHandler implements IItemHandlerUpdate {
	final XUTile tile;

	public XUTileItemStackHandler(int size, XUTile tile) {
		super(size);
		this.tile = tile;
	}

	public XUTileItemStackHandler(XUTile tile) {

		this.tile = tile;
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	protected void onContentsChanged(int slot) {
		tile.markDirty();
	}

	@Override
	public void onChange(int index) {
		onContentsChanged(index);
	}
}
