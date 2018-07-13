package image_gen;

import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImgGen {
	public static void createImage() {
		BufferedImage grass = readMCTexture("blocks/grass_top");
		int n = 16;
		BufferedImage[] outputs = new BufferedImage[n];

		int l = 256, u = 0;
		for (int i : grass.getRGB(0, 0, grass.getWidth(), grass.getHeight(), null, 0, grass.getWidth())) {
			int b = ColorHelper.brightness(i);
			l = Math.min(l, b);
			u = Math.max(u, b);
		}

		for (int i = 0; i < n; i++) {

			BufferedImage output = copyImage(grass);
			outputs[i] = output;
			for (int x = 0; x < output.getWidth(); x++) {
				for (int y = 0; y < output.getHeight(); y++) {
					int b = ColorHelper.brightness(grass.getRGB(x, y));
					b = l + ((b - l + (i * (u - l)) / n) % (u - l));
					output.setRGB(x, y, ColorHelper.makeGray(b));
				}
			}
		}

		outputImage("grass", grass);
		BufferedImage img = joinImages(outputs);
		outputImage("grassAnim", img);
		LogHelper.info("Done");
	}

	public static int sampleInterpolate(BufferedImage image, float x, float y, boolean interpAlpha) {
		int width = image.getWidth();
		int height = image.getHeight();

		float weight = 0;
		float[] result = new float[]{0, 0, 0, 0};

		int mx = (int) x;
		int my = (int) y;

		for (int dx = mx; dx <= (mx + 1); dx++) {
			for (int dy = my; dy <= (my + 1); dy++) {
				float w = (1 - Math.abs(x - dx)) * (1 - Math.abs(y - dy));
				float[] col = getColWrap(image, dx, dy);
				if (interpAlpha || (col[0] > 0.1)) {
					weight += w;
					for (int i = 0; i < 4; i++) {
						result[i] += col[i] * w;
					}
				}
			}
		}
		if (weight == 0) return 0;

		for (int i = 0; i < 4; i++) {
			result[i] = result[i] / weight;
		}

		return ColorHelper.floatsToCol(result);
	}

	public static float[] getColWrap(BufferedImage image, int x, int y) {
		int w = image.getWidth();
		int h = image.getHeight();
		int rgb = image.getRGB((x + 2 * w) % w, (y + 2 * h) % h);
		return ColorHelper.colToFloat(rgb);
	}

	public static BufferedImage joinImages(BufferedImage... images) {
		int w = 0;
		int h = 0;
		for (BufferedImage image : images) {
			w = Math.max(w, image.getWidth());
			h += image.getHeight();
		}

		BufferedImage image = newBufferedImage(w, h);

		int y = 0;
		for (BufferedImage im : images) {
			int width = im.getWidth();

			image.setRGB(0, y, width, im.getHeight(),
					im.getRGB(0, 0, width, im.getHeight(), null, 0, width)
					, 0, width);

//			image.setRGB(0, y, width, im.getHeight(), image.getRGB(0, 0, width, im.getHeight(), null, 0, im.getWidth()), 0, width);
			y += im.getHeight();
		}

		return image;
	}

	public static BufferedImage newBufferedImage(int w, int h) {
		return ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, h);
	}

	public static BufferedImage readMCTexture(String texture) {
		ResourceLocation resourceLocation = new ResourceLocation(texture);
		resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), "textures/" + resourceLocation.getResourcePath() + ".png");
		try {
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);

			BufferedImage readImage = TextureUtil.readBufferedImage(resource.getInputStream());

			return copyImage(readImage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static BufferedImage copyImage(BufferedImage readImage) {
		int height = readImage.getHeight();
		int width = readImage.getWidth();
		BufferedImage image = newBufferedImage(width, height);

		image.setRGB(0, 0, width, height,
				readImage.getRGB(0, 0, width, height, null, 0, width)
				, 0, width);
		return image;
	}

	public static void outputImage(String s, BufferedImage image) {
		File f = new File(new File(Minecraft.getMinecraft().mcDataDir, "xutexture"), s + ".png");

		try {
			if (!f.getParentFile().exists() && !f.getParentFile().mkdirs())
				return;

			if (!f.exists() && !f.createNewFile())
				return;

			ImageIO.write(image, "png", f);
		} catch (IOException e) {
			LogHelper.info("Unable to output " + s);
			e.printStackTrace();
		}
	}
}
