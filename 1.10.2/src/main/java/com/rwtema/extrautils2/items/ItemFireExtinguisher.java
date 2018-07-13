package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.PositionPool;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Random;

public class ItemFireExtinguisher extends XUItemFlat {
	@Override
	public void registerTextures() {
		Textures.register("fire_extinguisher");
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		return "fire_extinguisher";
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entityIn, int count) {

		if (!(entityIn instanceof EntityPlayer)) return;
		EntityPlayer playerIn = (EntityPlayer) entityIn;
		World worldIn = playerIn.world;

		if ((count & 3) != 0 && !worldIn.isRemote) return;
		float pitch = playerIn.rotationPitch;
		float yaw = playerIn.rotationYaw;
		double x0 = playerIn.posX;
		double y0 = playerIn.posY + (double) playerIn.getEyeHeight();
		double z0 = playerIn.posZ;
		Vec3d startPos = new Vec3d(x0, y0, z0);
		float f2 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-pitch * 0.017453292F);
		float dy = MathHelper.sin(-pitch * 0.017453292F);
		float dx = f3 * f4;
		float dz = f2 * f4;

		if (worldIn.isRemote) {
			Random r = worldIn.rand;
			for (int i = 0; i < 10; i++) {
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
						x0,
						(playerIn.posY + 2 * y0) / 3,
						z0,
						dx + r.nextGaussian() * 0.2,
						dy + r.nextGaussian() * 0.2,
						dz + r.nextGaussian() * 0.2);
			}
			return;
		}

		double reach = 8.0D;
		Vec3d finalPos = startPos.addVector((double) dx * reach, (double) dy * reach, (double) dz * reach);

		RayTraceResult rayTraceResult = worldIn.rayTraceBlocks(startPos, finalPos, false, true, true);
		if (rayTraceResult != null) {
			finalPos = rayTraceResult.hitVec;
			reach = finalPos.distanceTo(startPos);
		}

		PositionPool pool = new PositionPool();
		LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
		BlockPos prevPos = null;
		for (double t = 0; t <= reach; t += 0.5) {
			BlockPos p = pool.getPos(
					(int) Math.round(startPos.x + t * dx),
					(int) Math.round(startPos.y + t * dy),
					(int) Math.round(startPos.z + t * dz));
			if (prevPos == p) continue;
			prevPos = p;
			for (int x = -2; x <= 2; x++) {
				for (int y = -2; y <= 2; y++) {
					for (int z = -2; z <= 2; z++) {
						BlockPos add = pool.add(p, x, y, z);
						positions.add(add);
					}
				}
			}
		}

		for (BlockPos position : positions) {
			if (worldIn.getBlockState(position).getBlock() == Blocks.FIRE) {
				worldIn.playEvent(playerIn, 1009, position, 0);
				worldIn.setBlockToAir(position);
			}
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32767;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}
}
