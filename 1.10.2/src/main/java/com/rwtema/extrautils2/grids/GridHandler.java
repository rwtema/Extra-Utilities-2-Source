package com.rwtema.extrautils2.grids;

import com.rwtema.extrautils2.utils.datastructures.WeakSet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.ReferenceQueue;

public class GridHandler {
	public final static ReferenceQueue<? super XUTileGrid> gridQueue = new ReferenceQueue<Object>();

	static WeakSet<XUTileGrid> pendingTiles = new WeakSet<>();

	@SubscribeEvent
	public void handlePendingTiles(TickEvent.ServerTickEvent event) {
		for (XUTileGrid pendingTile : pendingTiles) {
			if (pendingTile.isLoaded()) {
				pendingTile.loadIntoGrids();
			}
		}
		Object x;
		while ((x = gridQueue.poll()) != null) {
			TileGridRef<?> x1 = (TileGridRef<?>) x;
			for (Grid grid : x1.grids) {
				grid.destroy();
			}
		}
	}
}
