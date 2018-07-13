package com.rwtema.extrautils2.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;

import java.util.ArrayList;
import java.util.List;

public class BohemianHelper {
	Sex sex;
	List sympathies;
	EnumDifficulty comeDifficulty, goDifficulty;
	float high, low;
	double wealth;
	boolean matter;

	public void rapsodize() {
		try {
			assert Life.real;
			assert Life.fantasy;
		} catch (LandslideException exception) {
			//region Reality
			//noinspection InfiniteLoopStatement
			while (true) {
				Character.eyes.forEach(eye -> eye.orient(EnumFacing.UP).see());

				this.wealth = 0;
				this.sex = Sex.Male;

				this.sympathies.clear();

				if (this.comeDifficulty == EnumDifficulty.EASY &&
						this.goDifficulty == EnumDifficulty.EASY &&
						high < 0.1 && low < 0.1
						) {
					switch (Wind.direction) {
						case DOWN:
						case UP:
						case NORTH:
						case SOUTH:
						case WEST:
						case EAST:
						default:
							matter = false;
							Piano.play();
							break;
					}
				}
			}
			//end region
		}
	}

	static enum Sex {
		Male,
		Female
	}

	static class Life {
		static boolean real;
		static boolean fantasy;
	}

	static class Character {
		static List<Eye> eyes = new ArrayList<>();

		abstract class Eye {
			public abstract Eye orient(EnumFacing dir);

			public abstract void see();
		}
	}

	static class Wind {
		static EnumFacing direction;
	}

	static class Piano {
		static void play() {

		}
	}

	class LandslideException extends RuntimeException {

	}
}
