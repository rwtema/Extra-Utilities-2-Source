package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.asm.Lighting;
import com.rwtema.extrautils2.lighting.ILight;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TileEvil extends XUTile implements ILight, ITESRHook {
	NBTSerializable.Float range = registerNBT("Range", new NBTSerializable.Float(96));

	@Override
	public World getLightWorld() {
		return world;
	}

	@Override
	public float getLightOffset(BlockPos lightPos, EnumSkyBlock type) {
		int dx = lightPos.getX() - pos.getX();
		int dz = lightPos.getZ() - pos.getZ();
		int dy = lightPos.getY() - pos.getY();
		float ri = dx * dx + dy * dy + dz * dz;
		float value = range.value;
		if (ri >= (value * value)) return 0;
		ri = MathHelper.sqrt(ri);
		float r = value - ri;
		if (r > 16) r = 16;

		float v = (float) Math.atan2(dx, dz);
		float v2 = (1 + MathHelper.cos(ri / 8F + v * 10F)) / 2;

		return -Math.min(r * v2, r) + Math.min(0, -(value / 3) + ri);
	}

	@Override
	public EnumSkyBlock[] getLightType() {
		return new EnumSkyBlock[]{EnumSkyBlock.BLOCK, EnumSkyBlock.SKY};
	}

	@Override
	public void invalidate() {
		super.invalidate();
		Lighting.unregister(this, Lighting.negLights);
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void onLoad() {
		Lighting.register(this, Lighting.negLights);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		Lighting.unregister(this, Lighting.negLights);
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		packet.writeFloat(range.value);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		range.value = packet.readFloat();
		updateLight();
	}

	private void updateLight() {
		int r = (int) Math.ceil(range.value);
		world.markBlockRangeForRenderUpdate(
				pos.getX() - r,
				pos.getY() - r,
				pos.getZ() - r,
				pos.getX() + r,
				pos.getY() + r,
				pos.getZ() + r);
	}

	@Override
	public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer) {
//		IBakedModel model = blockRenderer.getModelFromBlockState(BlockStates.enchanting_table, world, pos);
//		renderBakedModel(world, renderer, blockRenderer, model);
	}

	@Override
	public void preRender(int destroyStage) {

	}

	@Override
	public void postRender(int destroyStage) {

	}
}
