package com.rwtema.extrautils2.utils.helpers;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ISidedFunction;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

public class WorldHelper {
	private static final ISidedFunction<World, Float> getSunBrightness = new ISidedFunction<World, Float>() {
		@Override
		public Float applyServer(World world) {
			float f = world.getCelestialAngle(0);
			float f1 = 1.0F - (MathHelper.cos(f * (float) Math.PI * 2.0F) * 2.0F + 0.2F);
			f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
			f1 = 1.0F - f1;
			f1 = (float) ((double) f1 * (1.0D - (double) (world.getRainStrength(0) * 5.0F) / 16.0D));
			f1 = (float) ((double) f1 * (1.0D - (double) (world.getThunderStrength(0) * 5.0F) / 16.0D));
			return f1 * 0.8F + 0.2F;
		}

		@Override
		public Float applyClient(World world) {
			return world.getSunBrightness(0);
		}
	};

	public static float getSunBrightness(World world) {
		if (!CompatHelper.hasSky(world)) return 0;
		return ExtraUtils2.proxy.apply(getSunBrightness, world);
	}

	public static float getMoonBrightness(World world) {
		float f = world.getCelestialAngle(0);
		float f1 = -(MathHelper.cos(f * (float) Math.PI * 2.0F) * 2.0F + 0.2F);
		return MathHelper.clamp(f1, 0.0F, 1.0F);
	}

	public static boolean isFullMoon(World world) {
		return world.getCurrentMoonPhaseFactor() >= 0.9F;
	}


	public static TileEntity getTileEntityThreadSafe(World world, BlockPos pos) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
		TileEntity tileEntity = map.get(pos);
		if (tileEntity == null || tileEntity.isInvalid()) return null;
		return tileEntity;
	}

	public static void markBlockForUpdate(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 0);
	}
}
