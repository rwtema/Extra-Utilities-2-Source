package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.blocks.BlockScreen;
import com.rwtema.extrautils2.tile.TileScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ImgurTextureHolder {

	public static final ResourceLocation loading_texture = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/screen_loading.png");
	public static final ResourceLocation noSignal_texture = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/screen_no_signal.png");
	public static final ResourceLocation error_texture = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/screen_error.png");
	private static final Pattern patternControlCode = TileScreen.illegalPatternControlCode;
	private static final Logger logger = LogManager.getLogger();
	private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
	public static ImgurTextureHolder _default = new ImgurTextureHolder(loading_texture);
	private static HashMap<String, ImgurTextureHolder> holder = new HashMap<>();
	public ResourceLocation resourceLocation;
	public boolean deleteOldTexture;
	public SimpleTexture img;
	public BufferedImage image;
	public int width = 32;
	public int height = 32;
	public float u0 = 0, u1 = 1, v0 = 0, v1 = 1;

	public ImgurTextureHolder(ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public ImgurTextureHolder(String key) {

		key = patternControlCode.matcher(key).replaceAll("");
		String url = "https://i.imgur.com/" + key + ".png";

		img = new XUDownloadImageData(null, url, loading_texture, new IImageBuffer() {
			@Nonnull
			@Override
			public BufferedImage parseUserSkin(@Nonnull BufferedImage image) {
				if (image == null) return null;
				int w = image.getWidth();
				int h = image.getHeight();
				width = nextPower2(w);
				height = nextPower2(h);
				BufferedImage newImage = new BufferedImage(width, height, 2);

				Graphics graphics = newImage.getGraphics();
				int x = (width - w) / 2;
				int y = (height - h) / 2;

				u0 = ((float) x) / width;
				u1 = ((float) x + w) / width;
				v0 = ((float) y) / height;
				v1 = ((float) y + h) / height;

				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, 0, width, height);
				graphics.drawImage(image, x, y, null);
				graphics.dispose();

				ImgurTextureHolder.this.image = newImage;
				return newImage;
			}

			@Override
			public void skinAvailable() {

			}
		});

		resourceLocation = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "texures/imgur/" + key);
		Minecraft.getMinecraft().renderEngine.loadTexture(resourceLocation, img);
	}

	public static ImgurTextureHolder getTex(String tex) {
		if (tex.length() == 0) return _default;
		ImgurTextureHolder imgurTextureHolder = holder.get(tex);
		if (imgurTextureHolder != null) return imgurTextureHolder;
		imgurTextureHolder = new ImgurTextureHolder(tex);
		holder.put(tex, imgurTextureHolder);
		return imgurTextureHolder;
	}

	public static int nextPower2(int v) {
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		return v;
	}

	public ResourceLocation getResourceLocationForBinding() {
		if (deleteOldTexture) {
			img.deleteGlTexture();
			deleteOldTexture = false;
		}
		return resourceLocation;
	}

	public class XUDownloadImageData extends SimpleTexture {
		private final File cacheFile;
		private final String imageUrl;
		private final IImageBuffer imageBuffer;
		private BufferedImage bufferedImage;
		private Thread imageThread;
		private boolean textureUploaded;

		public XUDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn) {
			super(textureResourceLocation);
			this.cacheFile = cacheFileIn;
			this.imageUrl = imageUrlIn;
			this.imageBuffer = imageBufferIn;
		}

		private void checkTextureUploaded() {
			if (!this.textureUploaded) {
				if (this.bufferedImage != null) {
					if (this.textureLocation != null) {
						this.deleteGlTexture();
					}

					TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
					this.textureUploaded = true;
				}
			}
		}

		public int getGlTextureId() {
			this.checkTextureUploaded();
			return super.getGlTextureId();
		}

		public void setBufferedImage(BufferedImage bufferedImageIn) {
			this.bufferedImage = bufferedImageIn;

			if (this.imageBuffer != null) {
				this.imageBuffer.skinAvailable();
			}
		}

		public void loadTexture(IResourceManager resourceManager) throws IOException {
			if (this.bufferedImage == null && this.textureLocation != null) {
				super.loadTexture(resourceManager);
			}

			if (this.imageThread == null) {
				if (this.cacheFile != null && this.cacheFile.isFile()) {
					logger.debug("Loading http texture from local cache ({})", this.cacheFile);

					try {
						this.bufferedImage = ImageIO.read(this.cacheFile);

						if (this.imageBuffer != null) {
							this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
						}
					} catch (IOException ioexception) {
						logger.error("Couldn\'t load texture " + this.cacheFile, ioexception);
						this.loadTextureFromServer();
					}
				} else {
					this.loadTextureFromServer();
				}
			}
		}

		protected void loadTextureFromServer() {
			this.imageThread = new Thread("XU Texture Downloader #" + threadDownloadCounter.incrementAndGet()) {
				public void run() {
					HttpURLConnection httpurlconnection = null;
					logger.debug("Downloading http texture from {} to {}", XUDownloadImageData.this.imageUrl, XUDownloadImageData.this.cacheFile);

					try {
						httpurlconnection = (HttpURLConnection) (new URL(XUDownloadImageData.this.imageUrl)).openConnection(Minecraft.getMinecraft().getProxy());
						httpurlconnection.setDoInput(true);
						httpurlconnection.setDoOutput(false);
						httpurlconnection.connect();

						if (httpurlconnection.getResponseCode() / 100 == 2) {
							int contentLength = httpurlconnection.getContentLength();
							if (BlockScreen.maxSize > 0 && contentLength > BlockScreen.maxSize) {
								logger.debug(contentLength + " is larger than " + BlockScreen.maxSize);
								ImgurTextureHolder.this.resourceLocation = error_texture;
								ImgurTextureHolder.this.deleteOldTexture = true;
							} else {
								BufferedImage bufferedimage;

								if (XUDownloadImageData.this.cacheFile != null) {
									FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), XUDownloadImageData.this.cacheFile);
									bufferedimage = ImageIO.read(XUDownloadImageData.this.cacheFile);
								} else {
									bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
								}

								if (XUDownloadImageData.this.imageBuffer != null) {
									bufferedimage = XUDownloadImageData.this.imageBuffer.parseUserSkin(bufferedimage);
								}

								XUDownloadImageData.this.setBufferedImage(bufferedimage);
							}
						} else {
							ImgurTextureHolder.this.resourceLocation = noSignal_texture;
							ImgurTextureHolder.this.deleteOldTexture = true;
						}
					} catch (Exception exception) {
						logger.error("Couldn\'t download http texture", exception);
						ImgurTextureHolder.this.resourceLocation = error_texture;
						ImgurTextureHolder.this.deleteOldTexture = true;

					} finally {
						if (httpurlconnection != null) {
							httpurlconnection.disconnect();
						}
					}
				}
			};
			this.imageThread.setDaemon(true);
			this.imageThread.start();
		}
	}
}
