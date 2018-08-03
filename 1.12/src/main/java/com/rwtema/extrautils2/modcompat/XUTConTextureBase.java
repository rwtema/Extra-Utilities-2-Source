package com.rwtema.extrautils2.modcompat;

import com.rwtema.extrautils2.fluids.TexturePlasma;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.datastructures.IntPair;
import com.rwtema.extrautils2.utils.helpers.CIELabHelper;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;
import slimeknights.tconstruct.library.client.texture.AbstractColoredTexture;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class XUTConTextureBase extends AbstractColoredTexture {
	static final IntPair[] directNeighbours = new IntPair[]{
			IntPair.of(1, 0),
			IntPair.of(0, 1),
			IntPair.of(-1, 0),
			IntPair.of(0, -1),
	};
	private final static IntPair[] offsets = {
			IntPair.of(0, 1),
			IntPair.of(1, 1),
			IntPair.of(1, 0),
			IntPair.of(1, -1),
			IntPair.of(0, -1),
			IntPair.of(-1, -1),
			IntPair.of(-1, 0),
			IntPair.of(-1, 1)
	};
	int min, max;
	private HashMap<IntPair, Integer> colorOverrides;
	private HashMap<IntPair, Integer> valueOverrides;
	private int[] colorPalette = null;

	protected XUTConTextureBase(ResourceLocation baseTexture, String spriteName) {
		super(baseTexture, spriteName);
	}

	protected XUTConTextureBase(String baseTextureLocation, String spriteName) {
		super(new ResourceLocation(baseTextureLocation), spriteName);
	}

	public static boolean[] invert(boolean[] data) {
		boolean[] data2 = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			data2[i] = !data[i];
		}
		return data2;
	}

	public static boolean[] filter(int[] data, int level) {
		boolean[] t = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			t[i] = data[i] == level;
		}
		return t;
	}

	public static boolean[] filterUp(int[] data, int level) {
		boolean[] t = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			t[i] = data[i] >= level;
		}
		return t;
	}

	public static boolean[] filterDown(int[] data, int level) {
		boolean[] t = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			t[i] = data[i] <= level;
		}
		return t;
	}

	private static int coord(int x, int y, int w, int h) {
		if (x < 0 || y < 0 || x >= w || y >= h)
			return -1;

		return y * w + x;
	}

	public static int getBrightness(int pixel) {
		return ColorHelper.brightness(
				TexturePlasma.directColorModel.getRed(pixel),
				TexturePlasma.directColorModel.getGreen(pixel),
				TexturePlasma.directColorModel.getBlue(pixel)
		);
	}

	private static void check(int[] edges, int[] data, int x, int y, int w, int h) {
		int coord = coord(x, y, w, h);
		int col = data[coord];
		edges[coord] = col == 0 || TexturePlasma.directColorModel.getAlpha(col) < 32 ? 0 : 1;
	}

	private static void logImg(int[] levels, int width, int height) {
		if (!LogHelper.isDeObf) return;

		StringBuilder builder = new StringBuilder("\n");
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int level = levels[coord(x, y, width, height)];
				if (level < 0)
					builder.append(' ');
				else
					builder.append(CollectionHelper.CHAR_DIGITS[level]);
			}
			builder.append('\n');
		}
		LogHelper.debug(builder.toString());
	}

	public static int[] getEdgeDist(int[] data, int width, int height) {
		int[] edges = new int[width * height];
		for (int i = 0; i < edges.length; i++) {
			edges[i] = -1;
		}
		for (int x = 0; x < width; x++) {
			check(edges, data, x, 0, width, height);
			check(edges, data, x, height - 1, width, height);
		}
		for (int y = 1; y < (height - 1); y++) {
			check(edges, data, 0, y, width, height);
			check(edges, data, width - 1, y, width, height);
		}

		for (int x = 1; x < (width - 1); x++) {
			for (int y = 1; y < (height - 1); y++) {
				int coord = coord(x, y, width, height);
				int col = data[coord];
				if (col == 0 || TexturePlasma.directColorModel.getAlpha(col) < 32) {
					edges[coord] = 0;
				}
			}
		}

		int[] prevValues = new int[edges.length];
		int[] curValues = edges;

		boolean incomplete;
		do {
			int[] t = prevValues;
			prevValues = curValues;
			curValues = t;
			incomplete = false;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int coord = coord(x, y, width, height);
					if (prevValues[coord] == -1) {
						incomplete = true;
						int n = -1;
						for (IntPair pair : directNeighbours) {
							int otherN = prevValues[coord(x + pair.x, y + pair.y, width, height)];
							if (otherN != -1) {
								if (n == -1) {
									n = otherN + 1;
								} else {
									n = Math.min(n, otherN + 1);
								}
							}
						}

						curValues[coord] = n;
					} else {
						curValues[coord] = prevValues[coord];
					}
				}
			}
//			logImg(curValues, width, height);
		} while (incomplete);

		return curValues;
	}

	public static boolean[] and(boolean[]... inputArrays) {
		int length = inputArrays[0].length;
		for (int i = 1; i < inputArrays.length; i++) {
			Validate.isTrue(inputArrays[i].length == length);
		}

		boolean[] c = new boolean[length];
		for (int i = 0; i < length; i++) {
			c[i] = true;
			for (boolean[] aT : inputArrays) {
				if (!aT[i]) {
					c[i] = false;
					break;
				}
			}
		}
		return c;
	}

	public static boolean[] or(boolean[]... inputArrays) {
		int length = inputArrays[0].length;
		for (int i = 1; i < inputArrays.length; i++) {
			Validate.isTrue(inputArrays[i].length == length);
		}

		boolean[] c = new boolean[length];
		for (int i = 0; i < length; i++) {
			for (boolean[] aT : inputArrays) {
				if (aT[i]) {
					c[i] = true;
					break;
				}
			}
		}
		return c;
	}

	public static int multPixel(int col, int b) {
		return (255 << 24) |
				MathHelper.clamp((TexturePlasma.directColorModel.getRed(col) * b) / 255, 0, 255) << 16 |
				MathHelper.clamp((TexturePlasma.directColorModel.getGreen(col) * b) / 255, 0, 255) << 8 |
				MathHelper.clamp((TexturePlasma.directColorModel.getBlue(col) * b) / 255, 0, 255);
	}

	public void logImg(boolean[] img) {
		if (!LogHelper.isDeObf) return;

		StringBuilder builder = new StringBuilder("\n");
		builder.append('/');
		for (int x = 0; x < width; x++) {
			builder.append('-');
		}
		builder.append('\\');
		for (int x = 0; x < width; x++) {
			builder.append('|');
			for (int y = 0; y < height; y++) {
				builder.append(img[coord(x, y)] ? '1' : ' ');
			}
			builder.append('|');
			builder.append('\n');
		}
		builder.append('\\');
		for (int x = 0; x < width; x++) {
			builder.append('-');
		}
		builder.append('/');
		LogHelper.debug(builder.toString());
	}

	public void logImg(int[] levels) {
		int width = this.width;
		int height = this.height;
		logImg(levels, width, height);
	}

	protected void addColorPalette(int... colorPalette) {
		this.colorPalette = colorPalette;
	}

	public void addOverrides(Map<IntPair, Integer> map, boolean[] filter, int color) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int coord = coord(x, y);
				if (filter[coord]) {
					map.put(IntPair.of(x, y), color);
				}
			}
		}
	}

	public boolean[] getCorners(boolean[] data) {
		boolean[] corners = new boolean[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = coord(x, y, width, height);
				if (!data[i]) continue;
				int an = -1;
				int n = 0;
				for (IntPair offset : offsets) {
					int coord2 = coord(x + offset.x, y + offset.y, width, height);
					if (coord2 != -1 && data[coord2]) {
						if (an == -1)
							an = n;
						n = 0;
					} else {
						n++;
						if (n == 5) break;
					}
				}
				if (an != -1) {
					n += an;
				}
				if (n >= 5) {
					corners[coord(x, y)] = true;
				}
			}
		}


		return corners;
	}

	public int[] getEdgeDist(int[] data) {
		int width = this.width;
		int height = this.height;
		return getEdgeDist(data, width, height);
	}

	protected IntPair toIntPair(int pxCoord) {
		int y = pxCoord / width;
		int x = pxCoord - (y * width);
		return IntPair.of(x, y);
	}

	@Nullable
	protected <T> T getMipmapValue(HashMap<IntPair, T> map, int pxCoord, int mipmap) {
		if (mipmap == 0) {
			return map.get(toIntPair(pxCoord));
		}

		int y = pxCoord / width;
		int x = pxCoord - (y * width);

		int n = 1 << mipmap;
		int prevD = -1;
		T curBest = null;
		for (int dx = 0; dx < n; dx++) {
			for (int dy = 0; dy < n; dy++) {
				IntPair pair = IntPair.of(x * n + dx, y * n + dy);
				T t = map.get(pair);
				if (t != null) {
					int d = (dx - n >> 2) * (dx - n >> 2) + (dy - n >> 2) * (dy - n >> 2);
					if (d == 0) return curBest;
					if (prevD == -1 || d < prevD) {
						curBest = t;
						prevD = d;
					}
				}
			}
		}

		return curBest;
	}

	@Override
	protected void processData(int[] data) {
		valueOverrides = new HashMap<>();
		colorOverrides = new HashMap<>();
		preProcess(data, valueOverrides, colorOverrides);
		getLimits(data);
		super.processData(data);
		colorPalette = null;
		valueOverrides = null;
		colorOverrides = null;

	}

	private void getLimits(int[] data) {
		min = 255;
		max = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int c = data[coord(x, y)];
				if (c == 0 || TexturePlasma.directColorModel.getAlpha(c) < 32) continue;

				IntPair coord = IntPair.of(x, y);
				if (valueOverrides.containsKey(coord) || colorOverrides.containsKey(coord)) continue;

				int brightness = getBrightness(c);

				min = Math.min(min, brightness);
				max = Math.max(max, brightness);
			}
		}
	}

	protected abstract void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride);

	protected boolean[] shift(boolean[] input, int dx, int dy) {
		boolean[] output = new boolean[input.length];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int outCoord = coord(x + dx, y + dy, width, height);
				if (outCoord != -1) {
					output[outCoord] = input[coord(x, y)];
				}
			}
		}
		return output;
	}

	protected boolean[] expand(boolean[] input) {
		boolean[] output = new boolean[input.length];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (input[coord(x, y)]) {
					output[coord(x, y)] |= true;
					for (IntPair offset : offsets) {
						int coord = coord(x + offset.x, y + offset.y, width, height);
						if (coord != -1) {
							output[coord] |= true;
						}
					}

				}
			}
		}
		return output;
	}

	protected int mean(int[] input) {
		int n = 0;
		int total = 0;
		boolean[] output = new boolean[input.length];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int col = input[coord(x, y)];
				if (TexturePlasma.directColorModel.getAlpha(col) < 32) {
					continue;
				}
				int red = TexturePlasma.directColorModel.getRed(col);
				int green = TexturePlasma.directColorModel.getGreen(col);
				int blue = TexturePlasma.directColorModel.getBlue(col);

				int brightness = ColorHelper.brightness(red, green, blue);

				total += brightness;
				n++;
			}
		}

		return total / n;
	}

	@Override
	protected int colorPixel(int pixel, int pxCoord) {
		Integer valueOverride = getMipmapValue(valueOverrides, pxCoord, 0);
		if (valueOverride != null) {
			return valueOverride;
		}

		if (ColorHelper.getA(pixel) <= 0) return 0;

		Integer colorOverride = getMipmapValue(colorOverrides, pxCoord, 0);

		if (colorOverride != null)
			return CIELabHelper.changeColor(pixel, colorOverride);

		if (colorPalette != null) {
			if (pixel == 0 || TexturePlasma.directColorModel.getAlpha(pixel) < 32) {
				return 0;
			}

			if (min == max) return colorPalette[0];

			int brightness = getBrightness(pixel);
			float intensity = (brightness - min) / (float) (max - min);
			return TexturePlasma.interpolate(intensity, colorPalette);
		}

		return pixel;
	}

}
