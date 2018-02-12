package com.rwtema.extrautils2.tile.tesr;

import com.rwtema.extrautils2.tile.XUTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;

public class XUTESRHook<T extends XUTile & ITESRHook> extends XUTESRBase<T> {

	private BlockRendererDispatcher blockRenderer;

	@Override
	protected int getDrawMode(T te) {
		return te.getDrawMode();
	}

	@Override
	public boolean isGlobalRenderer(T te) {
		return te.isGlobalRenderer();
	}

	@Override
	protected VertexFormat getVertexFormat(T te) {
		return te.getVertexFormat();
	}

	@Override
	public void preRender(T te, int destroyStage) {
		te.preRender(destroyStage);
	}

	@Override
	public void postRender(T te, int destroyStage) {
		te.postRender(destroyStage);
	}

	@Override
	public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer) {
		if (blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos pos = te.getPos();
		IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
		te.render(world, pos, x, y, z, partialTicks, destroyStage, renderer, blockRenderer);
	}
}
