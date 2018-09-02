package com.rwtema.extrautils2.tile;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TilePlacerScannerMiner extends TileAdvInteractor {
	TileMine miner = registerNBT("miner", new TileMine());
	TileScanner scanner = registerNBT("scanner", new TileScanner());
	TileUse user = registerNBT("user", new TileUse());

	public Iterable<XUTile> getComponents() {
		return ImmutableList.of(miner, scanner, user);
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return null;
	}

	@Override
	protected boolean operate() {
		setDataAdvInteract(user);
		setDataBase(scanner);
		setDataAdvInteract(miner);

		user.button.value = TileUse.Button.RIGHT_CLICK;
		user.mode.value = TileUse.Mode.PLACE_BLOCK;


		if (scanner.calcCurrentState()) {
			miner.operate();
		} else {
			user.operate();
		}

		return true;
	}

	private void setDataAdvInteract(TileAdvInteractor tile) {
		setDataBase(tile);
		tile.frequency = frequency;
		tile.active = active;

	}

	private void setDataBase(XUTile tile) {
		tile.setWorld(world);
		tile.setPos(pos);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
}
