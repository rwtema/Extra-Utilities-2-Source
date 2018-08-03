package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

public class TileLoaderManager {
	private static final WeakLinkedSet<XUTile> loadingTiles = new WeakLinkedSet<>();

	public static void loadTile(XUTile tile) {
		synchronized (loadingTiles) {
			loadingTiles.add(tile);
		}
	}

	@SubscribeEvent
	public void checkLoad(TickEvent.ServerTickEvent event) {
		synchronized (loadingTiles) {
			loadingTiles.removeIf(loadingTile -> loadingTile.isLoaded());
		}
	}
}
