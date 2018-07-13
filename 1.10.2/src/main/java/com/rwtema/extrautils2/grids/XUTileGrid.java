package com.rwtema.extrautils2.grids;

import com.rwtema.extrautils2.tile.XUTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class XUTileGrid extends XUTile {
	final HashMap<GridType, Grid> gridTypes = new HashMap<>();
	TileGridRef<XUTileGrid> myRef = new TileGridRef<>(this);

	protected XUTileGrid(GridType... gridTypes) {
		for (GridType gridType : gridTypes) {
			this.gridTypes.put(gridType, null);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (Grid grid : myRef.grids) {
			grid.destroy();
		}
		myRef.grids.clear();
	}

	public void loadIntoGrids() {
		List<XUTileGrid> xuTileGrids = adjacentTiles();

		for (Map.Entry<GridType, Grid> entry : gridTypes.entrySet()) {
			if (entry.getValue() == null || !entry.getValue().isValid) {
				GridType gridType = entry.getKey();
				Grid myGrid = null;
				for (XUTileGrid xuTileGrid : xuTileGrids) {
					Grid otherGrid = xuTileGrid.gridTypes.get(gridType);
					if (otherGrid != null) {
						if (myGrid == null) {
							myGrid = otherGrid;
						} else {
							myGrid = gridType.mergeGrids(myGrid, otherGrid);
						}
					}
				}

				if (myGrid == null) myGrid = gridType.createGrid();
				myGrid.add(this);
			}
		}
	}

	public List<XUTileGrid> adjacentTiles() {
		List<XUTileGrid> list = new ArrayList<>(2);
		for (EnumFacing enumFacing : EnumFacing.values()) {
			BlockPos offset = pos.offset(enumFacing);
			if (enumFacing == EnumFacing.UP || enumFacing == EnumFacing.DOWN || world.isBlockLoaded(offset)) {
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity instanceof XUTileGrid) {
					XUTileGrid tileGrid = (XUTileGrid) tileEntity;
					if (isUnblockedNeighbour(enumFacing, tileGrid) && tileGrid.isUnblockedNeighbour(enumFacing.getOpposite(), this))
						list.add(tileGrid);
				}
			}
		}
		return list;
	}

	protected boolean isUnblockedNeighbour(EnumFacing enumFacing, XUTileGrid otherTile) {
		return true;
	}

}
