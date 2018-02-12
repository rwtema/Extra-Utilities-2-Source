package image_gen;

import com.google.common.base.Throwables;
import com.rwtema.extrautils2.textures.ImgurTextureHolder;
import com.rwtema.extrautils2.utils.LogHelper;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureUtil;

public class Imgur {

	public void run() {
		try {
//			http://i.imgur.com/mgM9sLh.png
			String id = "mgM9sLh";
			String url = "https://i.imgur.com/" + id + ".png";
			URL versionFile = new URL(url);

			int fileSize = getFileSize(versionFile);

			BufferedInputStream reader = null;
			try {
				reader = new BufferedInputStream(versionFile.openStream());
				BufferedImage bufferedImage = TextureUtil.readBufferedImage(reader);
				ImgGen.outputImage("buffer0", bufferedImage);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					}catch(IOException ignored){

					}
				}
			}

			ThreadDownloadImageData img = new ThreadDownloadImageData(null, url, null, new IImageBuffer() {
				@Nonnull
				@Override
				public BufferedImage parseUserSkin(@Nonnull BufferedImage image) {
					if (image == null) return null;


					return image;
				}

				@Override
				public void skinAvailable() {
					LogHelper.info("ImageLoaded");
				}
			});
			img.loadTexture(Minecraft.getMinecraft().getResourceManager());

			ImgurTextureHolder imgurTextureHolder = new ImgurTextureHolder(id);
			imgurTextureHolder.img.loadTexture(Minecraft.getMinecraft().getResourceManager());


		} catch (IOException err) {
			err.printStackTrace();
			throw Throwables.propagate(err);
		}
	}

	private int getFileSize(URL url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}
}
