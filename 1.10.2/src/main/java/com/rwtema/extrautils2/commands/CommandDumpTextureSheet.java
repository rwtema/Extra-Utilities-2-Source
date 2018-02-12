package com.rwtema.extrautils2.commands;

import com.rwtema.extrautils2.utils.LogHelper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class CommandDumpTextureSheet extends CommandBase {

	@Nonnull
	@Override
	public String getName() {
		return "dumpTextureAtlas";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender p_71518_1_) {
		return "dumpTextureAtlas";
	}


	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		outputTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, "textures");
	}

	public void outputTexture(ResourceLocation locationTexture, String s) {
		int terrainTextureId = Minecraft.getMinecraft().renderEngine.getTexture(locationTexture).getGlTextureId();

		if (terrainTextureId == 0) return;

		int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		int[] pixels = new int[w * h];

		IntBuffer pixelBuf = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, pixelBuf);
		pixelBuf.limit(w * h);
		pixelBuf.get(pixels);

		BufferedImage image = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, h);
		image.setRGB(0, 0, w, h, pixels, 0, w);

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
