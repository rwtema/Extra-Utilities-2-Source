package com.rwtema.extrautils2.dimensions.deep_dark;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.blocks.BlockTeleporter;
import com.rwtema.extrautils2.dimensions.TeleporterBase;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class TeleporterDeepDark extends TeleporterBase {
	public TeleporterDeepDark(WorldServer world, int destDim, int startDim) {
		super(world, destDim, startDim);
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
		BlockPos teleporterDest = findTeleporterDest(new BlockPos(x, (int) Math.floor(entityIn.posY), z), BlockTeleporter.Type.DEEP_DARK);
		if (teleporterDest != null) {
			entityIn.setLocationAndAngles(teleporterDest.getX() + 0.5, teleporterDest.getY() + 1, teleporterDest.getZ() + 0.5, entityIn.rotationYaw, entityIn.rotationPitch);
			return true;
		}
		return false;
	}

	@Override
	public boolean makePortal(Entity entity) {
		double x = (int) Math.floor(entity.posX) + 0.5;
		double z = (int) Math.floor(entity.posZ) + 0.5;

		if (destDim == -1) {
			x /= 8;
			z /= 8;
		}

		if (startDim == -1) {
			x *= 8;
			z *= 8;
		}

		int y = 180;

		for (int dx = -3; dx <= 3; dx++)
			for (int dz = -3; dz <= 3; dz++)
				for (int dy = -7; dy <= 4; dy++) {
					BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
					if (dx == 0 && dy == -1 && dz == 0) {
						this.world.setBlockState(pos, XU2Entries.teleporter.value.getDefaultState().withProperty(BlockTeleporter.property_type, BlockTeleporter.Type.DEEP_DARK).withProperty(BlockTeleporter.property_unbreakable, true), 2);
						this.world.scheduleBlockUpdate(pos, XU2Entries.teleporter.value, 1, 0);
					} else if (dx == -3 || dx == 3 || (dy + Math.max(Math.abs(dx), Math.abs(dz))) <= -1 || dy == 4 || dz == -3 || dz == 3) {
						this.world.setBlockState(pos, BlockStates.COBBLESTONE);
					} else if ((dy + Math.max(Math.abs(dx), Math.abs(dz))) == 0 && (dx == 2 || dx == -2 || dz == 2 || dz == -2)) {
						this.world.setBlockState(pos, BlockStates.TORCH_UP);
					} else {
						this.world.setBlockState(pos, BlockStates.AIR);
					}
				}

		entity.setLocationAndAngles(x + 0.5, y, z + 0.5, entity.rotationYaw, 0.0F);
		entity.motionX = entity.motionY = entity.motionZ = 0.0D;


		return true;
	}

}
