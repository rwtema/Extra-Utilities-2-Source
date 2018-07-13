package com.rwtema.extrautils2.dimensions;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.blocks.BlockTeleporter;
import com.rwtema.extrautils2.tile.TileTeleporter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TeleporterBase extends Teleporter {
	protected final WorldServer world;
	protected final int destDim;
	protected final int startDim;

	public TeleporterBase(WorldServer world, int destDim, int startDim) {
		super(world);
		this.world = world;
		this.destDim = destDim;
		this.startDim = startDim;
	}


	@Override
	public void placeInPortal(@Nonnull Entity entityIn, float rotationYaw) {
		if (!this.placeInExistingPortal(entityIn, rotationYaw)) {
			this.makePortal(entityIn);
			this.placeInExistingPortal(entityIn, rotationYaw);
		}
	}

	@Override
	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
		double x = (int) Math.floor(entityIn.posX) + 0.5;
		double z = (int) Math.floor(entityIn.posZ) + 0.5;

		if (destDim == -1) {
			x /= 8;
			z /= 8;
		}

		if (startDim == -1) {
			x *= 8;
			z *= 8;
		}

		Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
		int y = Math.min(chunk.getTopFilledSegment() + 16, 255);

		BlockPos blockpos;
		BlockPos nextBlock;

		boolean foundFree = world.provider.isSurfaceWorld();

		for (blockpos = new BlockPos(x, y, z); blockpos.getY() >= 0; blockpos = nextBlock) {
			nextBlock = blockpos.down();
			IBlockState state = chunk.getBlockState(nextBlock);

			if (state.getMaterial().blocksMovement() || state.getMaterial() == Material.WATER) {
				if (foundFree) {
					entityIn.setLocationAndAngles(x, blockpos.getY(), z, entityIn.rotationYaw, entityIn.rotationPitch);
					return true;
				}
			} else {
				foundFree = true;
			}
		}

		entityIn.setLocationAndAngles(x, world.provider.getAverageGroundLevel() + 1, z, entityIn.rotationYaw, entityIn.rotationPitch);
		return false;
	}

	@Override
	public boolean makePortal(Entity entityIn) {


		return false;
	}

	@Nullable
	public BlockPos findTeleporterDest(@Nonnull BlockPos pos, @Nullable BlockTeleporter.Type type) {
		BlockPos closest = null;
		double minDist = 0;
		for (int x = (pos.getX() - 8) >> 4; x <= (pos.getX() + 8) >> 4; x++)
			for (int z = (pos.getZ() - 8) >> 4; z <= (pos.getZ() + 8) >> 4; z++) {
				Chunk chunk = world.getChunkFromChunkCoords(x, z);
				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
					TileEntity tileEntity = entry.getValue();
					if (tileEntity instanceof TileTeleporter) {
						BlockPos key = entry.getKey();

						if (type != null) {
							IBlockState state = world.getBlockState(key);
							if (state.getBlock() != XU2Entries.teleporter.value || state.getValue(BlockTeleporter.property_type) != type) {
								continue;
							}
						}

						if (key == null) continue;
						double dist = pos.distanceSq(key);
						if (closest == null || dist < minDist) {
							closest = key;
							minDist = dist;
						}
					}
				}
			}

		return closest;
	}
}
