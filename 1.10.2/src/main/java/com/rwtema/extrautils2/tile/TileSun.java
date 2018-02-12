package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.asm.Lighting;
import com.rwtema.extrautils2.lighting.ILight;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.LightMathHelper;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class TileSun extends TilePower implements ILight {
	private static final int range = 120;

	@Override
	public World getLightWorld() {
		return world;
	}

	@Override
	public float getLightOffset(BlockPos lightPos, EnumSkyBlock type) {
		float dx = lightPos.getX() - pos.getX();
		float dy = lightPos.getY() - pos.getY();
		float dz = lightPos.getZ() - pos.getZ();
		float mH = 1 - LightMathHelper.approxSqrt(dx * dx + dy * dy + dz * dz, range * range);
		if (mH < (0.06666665f)) return 0;
		Chunk chunk = world.getChunkFromBlockCoords(lightPos);
		int lightFor = chunk.getLightFor(EnumSkyBlock.SKY, lightPos);
		return 0;
	}

	public float sqr(float a) {
		return a * a;
	}

	@Override
	public EnumSkyBlock[] getLightType() {
		return new EnumSkyBlock[]{EnumSkyBlock.BLOCK};
	}

	@Override
	public void onPowerChanged() {
		markForUpdate();
	}

	@Override
	public float getPower() {
		return 16;
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		boolean b = packet.readBoolean();
		if (b != active) {
			active = b;
			updateLight();
		}
	}


	private void updateLight() {
		world.markBlockRangeForRenderUpdate(
				pos.getX() - range,
				pos.getY() - range,
				pos.getZ() - range,
				pos.getX() + range,
				pos.getY() + range,
				pos.getZ() + range);
	}


	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		Lighting.unregister(this, getLightMap());
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		Lighting.unregister(this, getLightMap());
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Lighting.register(this, getLightMap());
	}

	protected WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> getLightMap() {
		return Lighting.plusLights;
	}

}
