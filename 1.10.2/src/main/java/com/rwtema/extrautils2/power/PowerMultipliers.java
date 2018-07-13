package com.rwtema.extrautils2.power;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PowerMultipliers {
	public final static IWorldPowerMultiplier SOLAR = new IWorldPowerMultiplier() {

		@Override
		public float multiplier(World world) {
			if (world == null) return 0;
			return !isDaytime(world) ? 0 : (world.isRaining() ? 0.95F : 1F);
		}


		@Override
		public String toString() {
			return "SOLAR";
		}
	};
	public final static IWorldPowerMultiplier LUNAR = new IWorldPowerMultiplier() {
		@Override
		public float multiplier(World world) {
			if (world == null) return 0;
			return (!isDaytime(world) ? 1 + world.getCurrentMoonPhaseFactor() * 0.25F : 0);
		}

		@Override
		public String toString() {
			return "LUNAR";
		}
	};

	public final static IWorldPowerMultiplier WIND = new IWorldPowerMultiplier() {
		private static final int TIME_POWER = 8;
		private static final int MASK = (1 << TIME_POWER) - 1;
		private static final float TIME_DIVISOR = 1 << TIME_POWER;

		private static final int RESULT_POW = 8;
		private static final int RESULT_MASK = (1 << RESULT_POW) - 1;
		private static final float RESULT_DIVISOR = 1 << RESULT_POW;

		@Override
		public float multiplier(World world) {
			if (world == null) return 0;
			long t = world.getTotalWorldTime();
			float v = ((int) t & MASK) / TIME_DIVISOR;
			long k = t >> TIME_POWER;
			k += world.provider.getDimension() * 31L;

			long a = k * k * 42317861L + k * 11L;
			long b = a + (2 * k + 1) * 42317861L + 11L;

			float ai = ((int) (a & RESULT_MASK)) / RESULT_DIVISOR;
			float bi = ((int) (b & RESULT_MASK)) / RESULT_DIVISOR;

			float v1 = ai + (bi - ai) * v;

			return 0.5F + v1 * 2F + (world.isRaining() ? 1 : 0) + (world.isThundering() ? 2 : 0);
		}


		@Override
		public String toString() {
			return "WIND";
		}
	};

	public static boolean isDaytime(World world) {
		return MathHelper.cos(world.getCelestialAngle(1) * ((float) Math.PI * 2.0F)) >= 0;
	}
}
