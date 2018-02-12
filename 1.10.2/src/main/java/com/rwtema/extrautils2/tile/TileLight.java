package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.asm.Lighting;
import com.rwtema.extrautils2.lighting.ILight;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class TileLight extends XUTile implements ILight {

	static final int r = 96;
	public boolean active = false;

	@Override
	public World getLightWorld() {
		return world;
	}


	private void updateLight() {
		world.markBlockRangeForRenderUpdate(
				pos.getX() - r,
				pos.getY() - r,
				pos.getZ() - r,
				pos.getX() + r,
				pos.getY() + r,
				pos.getZ() + r);
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		packet.writeBoolean(active);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		boolean b = packet.readBoolean();
		if (b != active) {
			active = b;
			updateLight();
		}
	}


	@Override
	public float getLightOffset(BlockPos pos, EnumSkyBlock type) {
		if (!active) return 0;
		return Math.max(15 - (getRange(pos) * 15) / r, 0);
	}

	public int getRange(BlockPos pos) {
		return Math.abs(this.pos.getX() - pos.getX()) + Math.abs(this.pos.getY() - pos.getY()) + Math.abs(this.pos.getZ() - pos.getZ());
	}

	@Override
	public EnumSkyBlock[] getLightType() {
		return new EnumSkyBlock[]{EnumSkyBlock.BLOCK};
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		Lighting.unregister(this, Lighting.plusLights);
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		Lighting.unregister(this, Lighting.plusLights);
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void onLoad() {
		Lighting.register(this, Lighting.plusLights);
	}
}
