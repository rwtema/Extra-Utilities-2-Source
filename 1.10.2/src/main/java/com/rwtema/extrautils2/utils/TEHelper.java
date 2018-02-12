package com.rwtema.extrautils2.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TEHelper {

	public static <K extends TileEntity> List<K> get(World world, BlockPos pos, int range, Predicate<TileEntity> predicate) {
		int x_min = pos.getX() - range;
		int x_max = pos.getX() + range;
		int z_min = pos.getZ() - range;
		int z_max = pos.getZ() + range;
		int y_min = pos.getY() - range;
		int y_max = pos.getY() + range;

		ImmutableList.Builder<K> builder = ImmutableList.builder();

		for (int x = x_min >> 4; x <= x_max >> 4; x++) {
			for (int z = z_min >> 4; z <= z_max >> 4; z++) {
				Chunk chunk = world.getChunkFromChunkCoords(x, z);
				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
					BlockPos key = entry.getKey();
					if (key.getX() >= x_min &&
							key.getZ() >= z_min &&
							key.getY() >= y_min &&
							key.getX() <= x_max &&
							key.getY() <= y_max &&
							key.getZ() <= z_max
							) {
						TileEntity value = entry.getValue();
						if (predicate.test(value)) {
							builder.add((K) value);
						}
					}
				}
			}
		}

		return builder.build();
	}
}
