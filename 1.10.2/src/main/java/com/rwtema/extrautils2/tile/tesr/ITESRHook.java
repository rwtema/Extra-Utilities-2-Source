package com.rwtema.extrautils2.tile.tesr;

import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public interface ITESRHook {
	@SideOnly(Side.CLIENT)
	void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer);

	@SideOnly(Side.CLIENT)
	default void preRender(int destroyStage) {

	}

	@SideOnly(Side.CLIENT)
	default void postRender(int destroyStage) {

	}

	@SideOnly(Side.CLIENT)
	default int getDrawMode() {
		return GL11.GL_QUADS;
	}

	@SideOnly(Side.CLIENT)
	default VertexFormat getVertexFormat() {
		return DefaultVertexFormats.BLOCK;
	}

	@SideOnly(Side.CLIENT)
	default boolean isGlobalRenderer() {
		return false;
	}

}
