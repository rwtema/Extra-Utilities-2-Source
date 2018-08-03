package com.rwtema.extrautils2.modcompat;

import com.rwtema.extrautils2.utils.datastructures.IntPair;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class XUTConTextureMagicWood extends XUTConTextureBase {


	static int[][] offsets = {
			{0, 1},
			{1, 1},
			{1, 0},
			{1, -1},
			{0, -1},
			{-1, -1},
			{-1, 0},
			{-1, 1}
	};

	protected XUTConTextureMagicWood(ResourceLocation baseTexture, String spriteName) {
		super(baseTexture, spriteName);
	}

	protected XUTConTextureMagicWood(String baseTextureLocation, String spriteName) {
		super(baseTextureLocation, spriteName);
	}

	private boolean[][] orwise(boolean[][] a, boolean[][] b) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				a[i][j] |= b[i][j];
			}
		}
		return a;
	}

	private boolean[][] mult(boolean[][] a, boolean[][] b) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				a[i][j] &= b[i][j];
			}
		}
		return a;
	}

	private boolean[][] multI(boolean[][] a, boolean[][] b) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				a[i][j] &= !b[i][j];
			}
		}
		return a;
	}

	private boolean[][] expand(boolean[][] base, int n) {
		boolean[][] output = expand(base);
		for (int i = 0; i < (n - 1); i++) {
			output = expand(output);
		}
		return output;
	}

	private boolean[][] contract(boolean[][] base, int n) {
		boolean[][] output = contract(base);
		for (int i = 0; i < (n - 1); i++) {
			output = contract(output);
		}
		return output;
	}

	@Override
	protected void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride) {
		int w = width;
		int h = height;

		int[][] pixels = new int[w][h];
		boolean[][] base = new boolean[w][h];

		int mean = 0;
		int div = 0;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int c = datum[coord(x, y)];
				pixels[x][y] = ColorHelper.brightness(c);
				boolean nottrans = c != 0 && ColorHelper.getA(c) > 64;
				if (nottrans) {
					base[x][y] = true;
					mean += pixels[x][y];
					div++;
				}
			}
		}

		if (div == 0)
			div = 1;

		mean = ((mean / div) * 2) / 4;


		final int n;
		if (w >= 256)
			n = 5;
		else if (w >= 128)
			n = 4;
		else if (w >= 64)
			n = 3;
		else if (w >= 32)
			n = 2;
		else
			n = 1;


		boolean[][] baseSilhouette = contract(base, n);

		boolean[][] interior1 = contract(baseSilhouette, n);

		boolean[][] baseCorners = multI(mult(expand(getCorners(baseSilhouette), n), baseSilhouette), interior1);
		boolean[][] baseCornersShift = orwise(orwise(shift(baseCorners, 0, -1), shift(baseCorners, -1, 0)), shift(baseCorners, -1, -1));


		boolean[][] interior2 = contract(interior1, 2 * n);


		boolean[][] interior3 = contract(interior2, n);
		boolean[][] interior4 = contract(interior3, n);

		boolean[][] interiorCorners = multI(mult(expand(getCorners(interior2), n), interior2), interior3);
		boolean[][] interiorCornersShift = orwise(orwise(shift(interiorCorners, -1, 0), shift(interiorCorners, 0, -1)), shift(interiorCorners, -1, -1));


		int trans = 0;
		int gold = 0xFFF9ED4F;
		int gold_highlight = 0xFFFFFF8B;
		int wood = 0xFF9D804F;
		int darkwood = 0xFF665132;

		int[][] outpixels = new int[w][h];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (!baseSilhouette[x][y]) { //transparent
					if (base[x][y])
						outpixels[x][y] = multPixel(darkwood, pixels[x][y] / 2);
					else
						outpixels[x][y] = trans;
				} else {
					if (!interior1[x][y]) { // main edge
						if (baseCorners[x][y]) {
							if (baseCornersShift[x][y])
								outpixels[x][y] = multPixel(gold, Math.max(pixels[x][y], mean));
							else
								outpixels[x][y] = multPixel(gold_highlight, Math.max(pixels[x][y], mean) + 5);
						} else {
							outpixels[x][y] = multPixel(darkwood, pixels[x][y]);
						}
					} else {
						if (!interior2[x][y] || interior3[x][y]) { //inner interior
							if (interior3[x][y] && !interior4[x][y])
								outpixels[x][y] = multPixel(wood, (pixels[x][y] * 3) / 4);
							else
								outpixels[x][y] =
										multPixel(wood, pixels[x][y]);
						} else {

							if (interiorCorners[x][y]) {
								if (interiorCornersShift[x][y])
									outpixels[x][y] = multPixel(gold, Math.max(pixels[x][y], mean));
								else
									outpixels[x][y] = multPixel(gold_highlight, Math.max(pixels[x][y], mean) + 5);
							} else
								outpixels[x][y] = multPixel(darkwood, pixels[x][y]);
						}

					}
				}

				valueOverride.put(IntPair.of(x, y), outpixels[x][y]);
			}
		}
	}

	private int clamp(int i) {
		return MathHelper.clamp(i, 0, 255);
	}

	public boolean get(boolean[][] img, int x, int y) {
		return x >= 0 && y >= 0 && x < img.length && y < img[x].length && img[x][y];

	}

	public boolean[][] shift(boolean[][] img, int dx, int dy) {
		int w = img.length;
		boolean[][] img2 = new boolean[w][w];

		for (int x = Math.max(-dx, 0); x < Math.min(w, w + dx); x++) {
			System.arraycopy(img[x + dx], Math.max(-dy, 0) + dy, img2[x], Math.max(-dy, 0), Math.min(w, w + dy) - Math.max(-dy, 0));
		}
		return img2;
	}

	public boolean[][] getCorners(boolean[][] img) {
		int w = img.length;
		boolean[][] img2 = new boolean[w][w];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < w; y++) {
				if (!img[x][y])
					continue;

				int an = -1;
				int n = 0;
				for (int[] offset : offsets) {
					if (get(img, x + offset[0], y + offset[1])) {
						if (an == -1)
							an = n;
						n = 0;
					} else {
						n++;
						if (n == 5) {
							break;
						}
					}
				}
				if (an != -1) {
					n += an;
				}
				if (n >= 5) {
					img2[x][y] = true;
				}
			}
		}

		return img2;
	}

	public boolean[][] contract(boolean[][] img) {
		int w = img.length;
		boolean[][] img2 = new boolean[w][w];

		for (int x = 0; x < w; x++) {
			System.arraycopy(img[x], 0, img2[x], 0, w);
		}

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < w; y++) {
				if (img[x][y]) {
					if (x == 0 || y == 0 || x == (w - 1) || y == (w - 1))
						img2[x][y] = false;
				} else {
					if (x > 0) img2[x - 1][y] = false;
					if (y > 0) img2[x][y - 1] = false;
					if (x < (w - 1)) img2[x + 1][y] = false;
					if (y < (w - 1)) img2[x][y + 1] = false;
				}
			}
		}

		return img2;
	}

	public boolean[][] expand(boolean[][] img) {
		int w = img.length;
		boolean[][] img2 = new boolean[w][w];

		for (int x = 0; x < w; x++) {
			System.arraycopy(img[x], 0, img2[x], 0, w);
		}

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < w; y++) {
				if (img[x][y]) {
					for (int[] offset : offsets) {
						int dx = x + offset[0];
						int dy = y + offset[1];

						if (dx >= 0 && dy >= 0 && dx < w && dy < w)
							img2[dx][dy] = true;
					}
				}
			}
		}

		return img2;
	}
}
